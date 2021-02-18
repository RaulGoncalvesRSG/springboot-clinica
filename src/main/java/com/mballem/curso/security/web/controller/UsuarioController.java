package com.mballem.curso.security.web.controller;

import java.util.Arrays;
import java.util.List;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mballem.curso.security.domain.Medico;
import com.mballem.curso.security.domain.Perfil;
import com.mballem.curso.security.domain.Usuario;
import com.mballem.curso.security.domain.enums.PerfilTipo;
import com.mballem.curso.security.service.MedicoService;
import com.mballem.curso.security.service.UsuarioService;

@Controller
@RequestMapping("u")
public class UsuarioController {
	
	@Autowired
	private UsuarioService service;
	@Autowired
	private MedicoService medicoService;
	
    //Arir cadastro de usuarios (medico/admin/paciente)
    @GetMapping("/novo/cadastro/usuario")				//Pag de cadastro de credenciais
    public String cadastroPorAdminParaAdminMedicoPaciente(Usuario usuario) {
        return "usuario/cadastro";
    }
    
    @GetMapping("/lista")
    public String listarUsuarios() {	//N tem arg pq apenas abre uma tela com tabela para listar os dados
        return "usuario/lista";
    }  

    // listar usuarios na datatables
    @GetMapping("/datatables/server/usuarios")
    public ResponseEntity<?> listarUsuariosDatatables(HttpServletRequest request) {
        return ResponseEntity.ok(service.buscarTodos(request));
    } 
    
    //Salvar cadastro de usuarios por administrador (as credenciais). Pode salvar os 3 tipos de perfis
    @PostMapping("/cadastro/salvar")
    public String salvarUsuarios(Usuario usuario, RedirectAttributes attr) {
    	List<Perfil> perfis = usuario.getPerfis();
    	
    	//Um paciente n pode ser médico ou admin		1L (admin), 2L (medico), 3L (paciente)
    	if (perfis.size() > 2 || 			
    			perfis.containsAll(Arrays.asList(new Perfil(1L), new Perfil(3L))) ||
    			perfis.containsAll(Arrays.asList(new Perfil(2L), new Perfil(3L)))) {

    		//"falha" - acessa o fragmento de alerta
    		attr.addFlashAttribute("falha", "Paciente não pode ser Admin e/ou Médico.");
    		attr.addFlashAttribute("usuario", usuario);
    	} else {
    		try {
    			service.salvarUsuario(usuario); 
    			attr.addFlashAttribute("sucesso", "Operação realizada com sucesso!");
    		} catch (DataIntegrityViolationException ex) {
    			//DataIntegrityViolationException é do Spring e trabalha com nível de BD
    			attr.addFlashAttribute("falha", "Cadastro não realizado, email já existente.");
			}
    	}
    	return "redirect:/u/novo/cadastro/usuario";		 //Leva para o primeiro método e reabre a pag de cadastro
    }
    
    //Pre edicao de credenciais de usuarios
    @GetMapping("/editar/credenciais/usuario/{id}")
    public ModelAndView preEditarCredenciais(@PathVariable("id") Long id) {
    	//Abre a própria pag de cadastro com os dados preenchidos
        return new ModelAndView("usuario/cadastro", "usuario", service.buscarPorId(id));
    }    
    
    //Pre edicao de cadastro de usuarios
    @GetMapping("/editar/dados/usuario/{id}/perfis/{perfis}")
    public ModelAndView preEditarCadastroDadosPessoais(@PathVariable("id") Long usuarioId, 
    												   @PathVariable("perfis") Long[] perfisId) {
    	/*Cada perfil q acessa este método terá um destino diferente como retorno
    	admin vai para pag de crenciais; médico vai para a pag de dados pessoais e admin n pode acessar
    	os dados pessoais do paciente*/
    	Usuario usuario = service.buscarPorIdEPerfis(usuarioId, perfisId);
    	
    	//O usuário é apenas admin
    	if (usuario.getPerfis().contains(new Perfil(PerfilTipo.ADMIN.getCod())) &&
    		!usuario.getPerfis().contains(new Perfil(PerfilTipo.MEDICO.getCod())) ) {
    		
    		return new ModelAndView("usuario/cadastro", "usuario", usuario);
    	} 
    	//Se o usuário é médico
    	else if (usuario.getPerfis().contains(new Perfil(PerfilTipo.MEDICO.getCod()))) {
    		/*O usuário pode ser um admin e um médico, porém pode n haver o médico cadastrado na tabela
    		 *de dados, apenas usuario com perfil de Medico. Então se ainda n tiver o médico cadastrado,
    		 retorna a pag para cadastrar o médico na tabela. O outro processo é se já existir o médico
    		 já estiver cadastrado, vai para a parte de editar os dados cadastrais do medico
    		 
    		 Se n tiver id, cria uma instância do Medico com o ID do Usuario. Se tiver, vai está atualizando,
    		 então passa a variável recupera na consulta buscarPorUsuarioId*/
    		Medico medico = medicoService.buscarPorUsuarioId(usuarioId);
    		
    		if (medico.hasNotId()) { 													//Salva o médico
    			return new ModelAndView("medico/cadastro", "medico", new Medico(new Usuario(usuarioId)));
    		}
    		else {
				return new ModelAndView("medico/cadastro", "medico", medico);			//Edita o médico
			}
    	} 
    	else if (usuario.getPerfis().contains(new Perfil(PerfilTipo.PACIENTE.getCod()))) {
    		ModelAndView model = new ModelAndView("error");				//Pag de destrino no parâmetro
    		model.addObject("status", 403);
    		model.addObject("error", "Área Restrita");
    		model.addObject("message", "Os dados de pacientes são restritos a ele.");
    		
    		return model;
    	}
        return new ModelAndView("redirect:/u/lista");
    }
    
    @GetMapping("/editar/senha")
    public String abrirEditarSenha() {
    	return "usuario/editar-senha";
    }
    
    @PostMapping("/confirmar/senha")
    public String editarSenha(@RequestParam("senha1") String s1, @RequestParam("senha2") String s2, 
    						  @RequestParam("senha3") String s3, @AuthenticationPrincipal User user,
    						  RedirectAttributes attr) {
    	
    	//Testa as senhas no lado do servidor para evitar q algúem burle o JS
    	if (!s1.equals(s2)) {
    		attr.addFlashAttribute("falha", "Senhas não conferem, tente novamente");
    		return "redirect:/u/editar/senha";					//O formulário aparecerá limpo
    	}
    	
    	Usuario usuario = service.buscarPorEmail(user.getUsername());
    	
    	//Verifica se a senha3 é igual à senha q está salva no BD
    	if(!UsuarioService.isSenhaCorreta(s3, usuario.getSenha())) {
    		attr.addFlashAttribute("falha", "Senha atual não confere, tente novamente");
    		return "redirect:/u/editar/senha";
    	}
    		
    	service.alterarSenha(usuario, s1);
    	attr.addFlashAttribute("sucesso", "Senha alterada com sucesso.");
    	return "redirect:/u/editar/senha";
    }
    
    //Abre página de novo cadastro de paciente
    @GetMapping("/novo/cadastro")
    public String novoCadastro(Usuario usuario) {
        return "cadastrar-se";
    } 
    
    //Pagina de resposta do cadatro de paciente
    @GetMapping("/cadastro/realizado")
    public String cadastroRealizado() {
        return "fragments/mensagem";
    }
    
    // rebece o form da página cadastrar-se
    @PostMapping("/cadastro/paciente/salvar")		//BindingResult faz validação no Back-end
    public String salvarCadastroPaciente(Usuario usuario, BindingResult result) throws MessagingException {
    	try {
    		service.salvarCadastroPaciente(usuario);
    	} catch (DataIntegrityViolationException ex) {
    	//DataIntegrityViolationException é lançada qnd tenta add um usuário q já existe no BD
    		result.reject("email", "Ops... Este e-mail já existe na base de dados.");
    		return "cadastrar-se";
    	}
    	return "redirect:/u/cadastro/realizado";
    }
    
    /*Recebe a requisicao de confirmacao de cadastro. Nesse momento no BD n tem o Paciente pq apenas o usuário
    do tipo Paciente foi criado. Para criar o Paciente referente à esse usuário, basta preencher os seus dados
    no form do sistema*/
    @GetMapping("/confirmacao/cadastro")
    public String respostaConfirmacaoCadastroPaciente(@RequestParam("codigo") String codigo, 
    												  RedirectAttributes attr) {    	
        service.ativarCadastroPaciente(codigo);
        //OBS: o primeiro param é uma variável da pag "cadastro"
        attr.addFlashAttribute("alerta", "sucesso");
        attr.addFlashAttribute("titulo", "Cadastro Ativado!");
        attr.addFlashAttribute("texto", "Parabéns, seu cadastro está ativo.");
        attr.addFlashAttribute("subtexto", "Singa com seu login/senha");
        
    	return "redirect:/login";
    } 
    
    //Abre a pagina de pedido de redefinicao de senha
    @GetMapping("/p/redefinir/senha")
    public String pedidoRedefinirSenha() {
    	return "usuario/pedido-recuperar-senha";
    }
    
    // form de pedido de recuperar senha
    @GetMapping("/p/recuperar/senha")
    public String redefinirSenha(String email, ModelMap model) throws MessagingException {
    	service.pedidoRedefinicaoDeSenha(email);
    
    	model.addAttribute("sucesso", "Em instantes você reberá um e-mail para "
    			+ "prosseguir com a redefinição de sua senha.");
    	model.addAttribute("usuario", new Usuario(email));
    	
    	return "usuario/recuperar-senha";
    }
    
    //Salvar a nova senha via recuperação de senha
    @PostMapping("/p/nova/senha")
    public String confirmacaoDeRedefinicaoDeSenha(Usuario usuario, ModelMap model) {
    	Usuario u = service.buscarPorEmail(usuario.getEmail());
    	
    	//Verifica se o código verificador é diferente ao código digitado pelo usuário
    	if (!usuario.getCodigoVerificador().equals(u.getCodigoVerificador())) {
    		model.addAttribute("falha", "Código verificador não confere.");
    		return "usuario/recuperar-senha";
    	}
    	
    	u.setCodigoVerificador(null);
    	service.alterarSenha(u, usuario.getSenha());
    	
    	model.addAttribute("alerta", "sucesso");
    	model.addAttribute("titulo", "Senha redefinida!");
    	model.addAttribute("texto", "Você já pode logar no sistema.");
    	
    	return "login";
    } 
}
