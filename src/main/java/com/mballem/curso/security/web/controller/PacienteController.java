package com.mballem.curso.security.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mballem.curso.security.domain.Paciente;
import com.mballem.curso.security.domain.Usuario;
import com.mballem.curso.security.service.PacienteService;
import com.mballem.curso.security.service.UsuarioService;

@Controller
@RequestMapping("pacientes")
public class PacienteController {
	
	@Autowired
	private PacienteService service;
	@Autowired
	private UsuarioService usuarioService;
	
	//Abrir pagina de dados pessoais do paciente
	@GetMapping("/dados")
	public String cadastrar(ModelMap model, @AuthenticationPrincipal User user) {
		Paciente paciente = service.buscarPorUsuarioEmail(user.getUsername());
		
		if (paciente.hasNotId()) {			//Caso n tenha ID, está fazendo insert e o form está vazio
			paciente.setUsuario(new Usuario(user.getUsername()));
		}
		
		model.addAttribute("paciente", paciente);		//Variável q chega no form
		return "paciente/cadastro";
	}
	
	//Salvar o form de dados pessoais do paciente com verificacao de senha
	@PostMapping("/salvar")
	public String salvar(Paciente paciente, ModelMap model, @AuthenticationPrincipal User user) {
		Usuario usuario = usuarioService.buscarPorEmail(user.getUsername());
		
		if (UsuarioService.isSenhaCorreta(paciente.getUsuario().getSenha(), usuario.getSenha())) {
			paciente.setUsuario(usuario);			//Faz relacionamento entre a tabela Usuário e Paciente
			service.salvar(paciente);
			model.addAttribute("sucesso", "Seus dados foram inseridos com sucesso.");
			
		} 
		else {
			model.addAttribute("falha", "Sua senha não confere, tente novamente.");
		}
		return "paciente/cadastro";					//Retorna para a própria pag
	}	
	
	//Editar o form de dados pessoais do paciente com verificacao de senha
	@PostMapping("/editar")
	public String editar(Paciente paciente, ModelMap model, @AuthenticationPrincipal User user) {
		Usuario u = usuarioService.buscarPorEmail(user.getUsername());
		
		if (UsuarioService.isSenhaCorreta(paciente.getUsuario().getSenha(), u.getSenha())) {
			service.editar(paciente);
			model.addAttribute("sucesso", "Seus dados foram editados com sucesso.");
		} 
		else {
			model.addAttribute("falha", "Sua senha não confere, tente novamente.");
		}
		return "paciente/cadastro";
	}	
}
