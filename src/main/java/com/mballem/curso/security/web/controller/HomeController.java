package com.mballem.curso.security.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	// abrir pagina home			pode acessar através de mais de uma url
	@GetMapping({"/", "/home"})			
	public String home() {
		return "home";
	}
	
	//Abrir pagina login
	@GetMapping({"/login"})
	public String login() {
		return "login";
	}	
	
	//Login inválido
	@GetMapping({"/login-error"})			//HttpServletRequest para capturar o tipo de exeção
	public String loginError(ModelMap model, HttpServletRequest request) {
		HttpSession session = request.getSession();				//Recupera o obj de sessão
		
		//Contém a classe referente a exceção q foi lançada
		String lastException = String.valueOf(session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION"));
		
		//Erro na tentativa de tentar fazer login mais doq o limite definido
		if (lastException.contains(SessionAuthenticationException.class.getName())) {
			model.addAttribute("alerta", "erro");
			model.addAttribute("titulo", "Acesso recusado!");
			model.addAttribute("texto", "Você já está logado em outro dispositivo.");
			model.addAttribute("subtexto", "Faça o logout ou espere sua sessão expirar.");
			
			return "login";
		}
		
		model.addAttribute("alerta", "erro");
		model.addAttribute("titulo", "Crendenciais inválidas!");
		model.addAttribute("texto", "Login ou senha incorretos, tente novamente.");
		model.addAttribute("subtexto", "Acesso permitido apenas para cadastros já ativados.");
		
		return "login";
	}
	
	//Acesso de sessão expirado
	@GetMapping({"/expired"})
	public String loginError(ModelMap model) {
		model.addAttribute("alerta", "erro");
		model.addAttribute("titulo", "Acesso negado!");
		model.addAttribute("texto", "Sua sessão expirou.");
		model.addAttribute("subtexto", "Você logou em outro dispositivo.");
	
		return "login";
	}
	
	//Acesso negado - quando o usuário logado tenta acessar uma área q ele n tem permissão de acesso
	@GetMapping({"/acesso-negado"})			//Com response consegue pegar o status da requisição
	public String acessoNegado(ModelMap model, HttpServletResponse response) {
		model.addAttribute("status", response.getStatus());
		model.addAttribute("error", "Acesso Negado");
		model.addAttribute("message", "Você não tem permissão para acesso a esta área ou ação.");
		
		return "error";
	}	
}
