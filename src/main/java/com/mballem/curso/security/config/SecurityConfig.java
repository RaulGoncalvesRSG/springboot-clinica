package com.mballem.curso.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import com.mballem.curso.security.domain.enums.PerfilTipo;
import com.mballem.curso.security.service.UsuarioService;

//Habilita o uso de anotações de métodos para a base de segurança. Bloqueia métodos para determinados perfis
@EnableGlobalMethodSecurity(prePostEnabled = true) 
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	//Para n precisar trabalhar com str e elimina a chance de digitar algo errado
	private static final String ADMIN = PerfilTipo.ADMIN.getDesc();
    private static final String MEDICO = PerfilTipo.MEDICO.getDesc();
    private static final String PACIENTE = PerfilTipo.PACIENTE.getDesc();
	
	@Autowired
	private UsuarioService usuarioService;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		//authorizeRequests inicia as configurações de qm vai ter acesso ou n dentro da aplicação
		http.authorizeRequests()
			//permitAll torna os acessos públicos liberados, signigica q a url n precisa de autenticação
			.antMatchers("/webjars/**", "/css/**", "/image/**", "/js/**").permitAll()
			.antMatchers("/", "/home", "/expired").permitAll()					//Todos perfis tem acesso à pag inicial
			.antMatchers("/u/novo/cadastro", "/u/cadastro/realizado", "/u/cadastro/paciente/salvar").permitAll()
			.antMatchers("/u/confirmacao/cadastro").permitAll()
			.antMatchers("/u/p/**").permitAll()
			
			//Acessos privados admin
			.antMatchers("/u/editar/senha", "/u/confirmar/senha").hasAnyAuthority(PACIENTE, MEDICO)
			.antMatchers("/u/**").hasAuthority(ADMIN)	
			
			//Acessos privados medicos
			.antMatchers("/medicos/especialidade/titulo/*").hasAnyAuthority(PACIENTE, MEDICO)
			.antMatchers("/medicos/dados", "/medicos/salvar", "/medicos/editar").hasAnyAuthority(MEDICO, ADMIN)
			//As 3 url acima vão estar bloqueadas embaixo, por isso em cima coloca hasAnyAuthority com MEDICO
			.antMatchers("/medicos/**").hasAuthority(MEDICO)			
			
			/*hasAuthority- pega a string do perfil de acordo com o BD. hasAnyAuthority recebe uma lista de perfis
			Apenas ADMIN vai conseguir acessar todas as urls de controller de usuário
			Se n quiser colocar **, basta colocar a url completa
			O Spring consegue fazer essa separação devido a instância de User, quando pega a lista de perfis do 
			usuário q está logado*/
			
			//Acessos privados pacientes
			.antMatchers("/pacientes/**").hasAuthority(PACIENTE)		
			.antMatchers("/especialidades/datatables/server/medico/*").hasAnyAuthority(MEDICO, ADMIN)
			.antMatchers("/especialidades/titulo").hasAnyAuthority(ADMIN, MEDICO, PACIENTE)
			
			//Acessos privados especialidades
			.antMatchers("/especialidades/**").hasAuthority(ADMIN)		
			
			.anyRequest().authenticated()
			.and()				//and concatena instruções de tipos diferentes
				.formLogin()
				.loginPage("/login")			  
				.defaultSuccessUrl("/", true)	  //Direciona para qual pag deve ir depois do sucesso de login
				.failureUrl("/login-error")		  //Direciona a pag quando há falha no login
				.permitAll()			//Todos usuários (msmo n logado) têm permissão para a pag de login e erro
			.and()
				.logout()
				.logoutSuccessUrl("/")				//Direciona para uma determinada pag depois do logout
				//Garante q o ID da sessão (JSESSIONID) n apareça na url em nenhum nevegador
				.deleteCookies("JSESSIONID")
			.and()
				.exceptionHandling()				//Captura a exceção quando tiver acesso negado
				//Nesse método passa a uri do controller no qual a exceção será tratada 
				.accessDeniedPage("/acesso-negado")
			.and()
				.rememberMe()  							 //rememberMe tem validade de 14 dias por padrão
				.rememberMeCookieName("clinica-spring-me")
				.tokenValiditySeconds(604800);			//1 dia = 86400s, 7 dias = 604800s
		
		validarLimiteLogin(http);
	}

	private void validarLimiteLogin(HttpSecurity http) throws Exception {
	//sessionManagement diz para a configuração q é para o spring olhar para a parte do gerenciamento de sessão
		http.sessionManagement()
			.maximumSessions(1)		//Quantos dispositivos são capazes de se conectar simultaneamente no app
			//Informa q qnd tiver logado em um disposivo, n irá conseguir logar em outro dispositivo
			.maxSessionsPreventsLogin(true)					//Se for false, consegue fazer login em outro
			//Pega as informações q o Bean recuperou da sessão para o gerenciamento
			.sessionRegistry(sessionRegistry());
	}

	@Bean		//O Bean pega a sessão q o spring possui referente a cada login q está sendo feito na aplicação
	public SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	}
	
	@Bean
	public ServletListenerRegistrationBean<?> ServletListenerRegistrationBean(){
		/*Com o HttpSessionEventPublisher, restria o servlet na app e esse servlet fica responsável por cuidar
		de todas as operações de login q estão sendo realizadas*/
		return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
	}
	
	@SuppressWarnings("unused")
	//Segundo login entra na sessão e remove a sessão existente
	private void invalidarPrimeiroLogin(HttpSecurity http) throws Exception {
		http.sessionManagement()
			.maximumSessions(1)
			.expiredUrl("/expired")
			.maxSessionsPreventsLogin(false)		 //Se for false, consegue fazer login em outro dispositivo
			.sessionRegistry(sessionRegistry());			
	
		http.sessionManagement()
			/*sessionFixation dá acesso a próxima operação a ser utilizada
			 * newSession cria a nova sessão do segundo login e invalida a sessão q já existia*/
			.sessionFixation()
			.newSession()
			//Para saber qual exceção já existia
			.sessionAuthenticationStrategy(SessionAuthenticationStrategy());
	}
	
	@Bean			
	public SessionAuthenticationStrategy SessionAuthenticationStrategy() {
		return new RegisterSessionAuthenticationStrategy(sessionRegistry());
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		/*userDetailsService - recebe um param do tipo UserDetailsService. 
		 A partir desse método, conseguimos informar qual é a criptografia e ele consegue acessar o resultado
		q ele vai ober a patir da instância passada em User (método loadUserByUsername). As senha salva será 
		comparada com a senha do formulário de login a partir deste método
		
		new BCryptPasswordEncoder().encode("root") gera mais de uma criptografia
		diferente e todas elas possuem o msmo valor de "root".
		
		passwordEncoder - informa o tipo de criptografia utilizada, precisa usar o msmo tipo de criptografia
		q foi usada para salvar a senha no momento do cadastro. A documentação indica o BCryptPasswordEncoder*/
		auth.userDetailsService(usuarioService).passwordEncoder(new BCryptPasswordEncoder());
	}
}
