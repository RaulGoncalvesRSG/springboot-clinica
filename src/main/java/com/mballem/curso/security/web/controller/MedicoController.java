package com.mballem.curso.security.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mballem.curso.security.domain.Medico;
import com.mballem.curso.security.domain.Usuario;
import com.mballem.curso.security.service.MedicoService;
import com.mballem.curso.security.service.UsuarioService;

@Controller 
@RequestMapping("medicos")
public class MedicoController {
	
	@Autowired
	private MedicoService service;
	@Autowired
	private UsuarioService usuarioService;

	//Abrir pagina de dados pessoais de medicos pelo MEDICO
	@GetMapping({"/dados"})
	public String abrirPorMedico(Medico medico, ModelMap model, @AuthenticationPrincipal User user) {
		/*Se o medico tem ID então ele veio do metodo de salvar ou editar, se n, a requisição veio 
		diretamente pelo link*/
		if (medico.hasNotId()) {
			medico = service.buscarPorEmail(user.getUsername());	//Pega o username logado
			model.addAttribute("medico", medico);
		}
		return "medico/cadastro";
	}
	
	//Salvar medico
	@PostMapping({"/salvar"})				//Recebe a variável medico com os dados preenchidos no form
	public String salvar(Medico medico, RedirectAttributes attr, @AuthenticationPrincipal User user) {
		/*Quando faz o insert de Medico logado como admin, o ID do usuário existe no obj Medico
		 Quando faz o insertde Medico logado como Medico, n tem o ID de usuario. Então verifica
		 qnd q essa operação está sendo feita pelo perfil do Medico*/
		
		/*Se o medico n tem ID e nem o Usuario, signigca q está fazendo insert pelo login de Medico e vamos ter
		q buscar pelo ID de Usuario na tabela usando o username do Medico*/
		if (medico.hasNotId() && medico.getUsuario().hasNotId()) {
			//Pega o obj do Usuario q está logado no sistema
			Usuario usuario = usuarioService.buscarPorEmail(user.getUsername());
			medico.setUsuario(usuario);
		}
		
		service.salvar(medico);
		attr.addFlashAttribute("sucesso", "Operação realizada com sucesso.");
		attr.addFlashAttribute("medico", medico);
		
		return "redirect:/medicos/dados";			//Reabre o form com os dados preenchidos
	}
	
	//Editar medico
	@PostMapping({"/editar"})			//Trabalha com as variávels de resposta através do RedirectAttributes
	public String editar(Medico medico, RedirectAttributes attr) {
		service.editar(medico);
		attr.addFlashAttribute("sucesso", "Operação realizada com sucesso.");
		attr.addFlashAttribute("medico", medico);
		
		return "redirect:/medicos/dados";		
	}
	
	//Excluir especialidade - Apenas o médico pode excluir sua especialidade
	@GetMapping({"/id/{idMed}/excluir/especializacao/{idEsp}"})
	public String excluirEspecialidadePorMedico(@PathVariable("idMed") Long idMed, 
						 @PathVariable("idEsp") Long idEsp, RedirectAttributes attr) {
		
		if ( service.existeEspecialidadeAgendada(idMed, idEsp) ) {
			attr.addFlashAttribute("falha", "Tem consultas agendadas, exclusão negada.");
		} 
		else {		
			service.excluirEspecialidadePorMedico(idMed, idEsp);
			attr.addFlashAttribute("sucesso", "Especialidade removida com sucesso.");
		}
		return "redirect:/medicos/dados";		
	}
	
	//Buscar medicos por especialidade via ajax
	@GetMapping("/especialidade/titulo/{titulo}")				//Recebe uma especialidade
	public ResponseEntity<?> getMedicosPorEspecialidade(@PathVariable("titulo") String titulo) {
		return ResponseEntity.ok(service.buscarMedicosPorEspecialidade(titulo));
	}
}
