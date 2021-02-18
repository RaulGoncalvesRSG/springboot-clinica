package com.mballem.curso.security.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

import com.mballem.curso.security.datatables.Datatables;
import com.mballem.curso.security.datatables.DatatablesColunas;
import com.mballem.curso.security.domain.Perfil;
import com.mballem.curso.security.domain.Usuario;
import com.mballem.curso.security.domain.enums.PerfilTipo;
import com.mballem.curso.security.exception.AcessoNegadoException;
import com.mballem.curso.security.repository.UsuarioRepository;

/*Qnd submeter o form, o SS vai identificar q está tentando logar no sistema. Pega as credenciais (user
 *e senha) e vai procurar por uma implementação da classe UserDetailsService q tem o método 
 *loadUserByUsername*/
@Service
public class UsuarioService implements UserDetailsService {

	@Autowired
	private UsuarioRepository repository;
	
	@Autowired
	private Datatables datatables;
	
	@Autowired
	private EmailService emailService;
	
	@Transactional(readOnly = true)
	public Usuario buscarPorEmail(String email) {
		return repository.findByEmail(email);
	}
	
	/*Quando n usa o @Transactional, gera uma exceção no usuario.getPerfis() pq está tentando pegar os perfis
	 *sendo q a transação com o banco no método buscarPorEmail foi encerrada. Então usa a anotação para manter
	 a transação ativa e ser possível pegar os perfis do usuário*/
	@Transactional(readOnly = true)				
	@Override		//UserDetails é a classe q verifica se o usuário está logado no sistema ou n
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		/*User é uma classe do spring q implementa UserDetails. Ele pede o user, senha e os perfis do user.
		Testa se a senha é válida e vai testar qnd tentar acessar cada uma das partes do sistema se o perfil
		dá ou n permissão para q ele acesse*/
		Usuario usuario = buscarPorEmailEAtivo(username)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario " + username + " não encontrado."));
		/*Msmo com a msg do UsernameNotFoundException, o método executado será o failureUrl do SecurityConfig
		Isso é uma segurança a mais caso dê algum problema na pag de erro do failureUrl*/
		
		return new User(
			usuario.getEmail(),
			usuario.getSenha(),
			AuthorityUtils.createAuthorityList(getAtuthorities(usuario.getPerfis()))
		);
	}
	
	//Retorna lista de roles/papeis/perfis				createAuthorityList recebe uma lista de strings
	private String[] getAtuthorities(List<Perfil> perfis) {
		String[] authorities = new String[perfis.size()];
		
		for (int i = 0; i < perfis.size(); i++) {
			authorities[i] = perfis.get(i).getDesc();		//Pega a descrição do perfil
		}
		return authorities;
	}
	
	@Transactional(readOnly = true)
	public Map<String, Object> buscarTodos(HttpServletRequest request) {
		datatables.setRequest(request);
		datatables.setColunas(DatatablesColunas.USUARIOS);
		
		//Pega o resultado a partir da consulta
		Page<Usuario> page = datatables.getSearch().isEmpty()
				? repository.findAll(datatables.getPageable())	 //Tem apenas recursos de ordenação e paginação
				: repository.findByEmailOrPerfil(datatables.getSearch(), datatables.getPageable());
		
		return datatables.getResponse(page);
	}

	@Transactional(readOnly = false)
	public void salvarUsuario(Usuario usuario) {
		String crypt = new BCryptPasswordEncoder().encode(usuario.getSenha());
		usuario.setSenha(crypt);		//Salva com a senha criptografada

		repository.save(usuario); 	 	
	}

	@Transactional(readOnly = true)
	public Usuario buscarPorId(Long id) {
		return repository.findById(id).get();
	}

	@Transactional(readOnly = true)
	public Usuario buscarPorIdEPerfis(Long usuarioId, Long[] perfisId) {
		/*orElseThrow - Se existir um obj usuario dentro do Optional, retorna um Usuario
		Se a consulta n encontrou registro no BD, lança a exceção. OBS: a declaração da exceção
		precisa ser no formado lambda. Em vez de lançar NullPointException, irá lançar 
		UsernameNotFoundException e cair na classe ExceptionController*/
		
		return repository.findByIdAndPerfis(usuarioId, perfisId)
				.orElseThrow(() -> new UsernameNotFoundException("Usuário inexistente!"));
	}
	
	public static boolean isSenhaCorreta(String senhaDigitada, String senhaArmazenada) {
		//O método matches compara uma senha digitada com uma senha criptografada
		return new BCryptPasswordEncoder().matches(senhaDigitada, senhaArmazenada);
	}

	@Transactional(readOnly = false)			//false pq vai fazer uma alteração no BD
	public void alterarSenha(Usuario usuario, String senha) {
		usuario.setSenha(new BCryptPasswordEncoder().encode(senha));
		repository.save(usuario);		
	}
	
	@Transactional(readOnly = false)
	public void salvarCadastroPaciente(Usuario usuario) throws MessagingException {
		String crypt = new BCryptPasswordEncoder().encode(usuario.getSenha());
		usuario.setSenha(crypt);
		usuario.addPerfil(PerfilTipo.PACIENTE);				//Salva o usuário cadastrado como um PACIENTE
		repository.save(usuario);	
		
		emailDeConfirmacaoDeCadastro(usuario.getEmail());
	}
	
	@Transactional(readOnly = true)
	public Optional<Usuario> buscarPorEmailEAtivo(String email) {
		return repository.findByEmailAndAtivo(email);
	}
	
	/*Pega o email e transforma em código Base64 pq ele será enviado como uma parte da url
	N será enviado o email original pela url pq alguns servidores de email aceitam caracteres
	especiais q podem acabar causando algum problema na url*/
	public void emailDeConfirmacaoDeCadastro(String email) throws MessagingException {
		String codigo = Base64Utils.encodeToString(email.getBytes());
		emailService.enviarPedidoDeConfirmacaoDeCadastro(email, codigo);
	}
	
	@Transactional(readOnly = false)						//codigo é o email em formato base64
	public void ativarCadastroPaciente(String codigo) {
		String email = new String(Base64Utils.decodeFromString(codigo));
		Usuario usuario = buscarPorEmail(email);	    //Procura o usuário de acordo com o email codificado
		
		//Se n tiver ID, n existe no BD
		if (usuario.hasNotId()) {
			throw new AcessoNegadoException("Não foi possível ativar seu cadastro. Entre em "
					+ "contato com o suporte.");
		}
		usuario.setAtivo(true);				 //Faz um update no BD transformando o usuárioe em ativo	
	}

	@Transactional(readOnly = false)			//readOnly = false pq vai fazer um update no BD
	public void pedidoRedefinicaoDeSenha(String email) throws MessagingException {
		//Apenas usuário ativo pode recuperar senha 
		Usuario usuario = buscarPorEmailEAtivo(email)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario " + email + " não encontrado."));;
		
		//Os caracteres variam entre letras e números e possui tamanho de acordo com o valor informado
		String verificador = RandomStringUtils.randomAlphanumeric(6);
		
		usuario.setCodigoVerificador(verificador);
		
		emailService.enviarPedidoRedefinicaoSenha(email, verificador);
	}
}
