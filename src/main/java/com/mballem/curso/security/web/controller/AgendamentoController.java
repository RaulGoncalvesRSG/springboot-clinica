package com.mballem.curso.security.web.controller;

import java.time.LocalDate;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mballem.curso.security.domain.Agendamento;
import com.mballem.curso.security.domain.Especialidade;
import com.mballem.curso.security.domain.Paciente;
import com.mballem.curso.security.domain.enums.PerfilTipo;
import com.mballem.curso.security.service.AgendamentoService;
import com.mballem.curso.security.service.EspecialidadeService;
import com.mballem.curso.security.service.PacienteService;

@Controller
@RequestMapping("agendamentos")
public class AgendamentoController {
	
	@Autowired
	private AgendamentoService service;
	@Autowired
	private PacienteService pacienteService;
	@Autowired
	private EspecialidadeService especialidadeService;	

	//Abre a pagina de agendamento de consultas
	@PreAuthorize("hasAnyAuthority('PACIENTE', 'MEDICO')")
	@GetMapping({"/agendar"})
	public String agendarConsulta(Agendamento agendamento) {
		return "agendamento/cadastro";		
	}
	
	//Busca os horarios livres, ou seja, sem agendamento
	//O lado cliente envia a data como str e precisa fazer uma converão para LocalDate
	@PreAuthorize("hasAnyAuthority('PACIENTE', 'MEDICO')")
	@GetMapping("/horario/medico/{id}/data/{data}")
	public ResponseEntity<?> getHorarios(@PathVariable("id") Long id,
										 @PathVariable("data") @DateTimeFormat(iso = ISO.DATE) LocalDate data) {
		return ResponseEntity.ok(service.buscarHorariosNaoAgendadosPorMedicoIdEData(id, data));
	}
	
	//Salvar consulta agendada
	@PreAuthorize("hasAuthority('PACIENTE')")
	@PostMapping({"/salvar"})
	public String salvar(Agendamento agendamento, RedirectAttributes attr, @AuthenticationPrincipal User user) {
		//Pega o paciente que está tentando fazer o agendamento de consulta
		Paciente paciente = pacienteService.buscarPorUsuarioEmail(user.getUsername());
		
		//Qnd envia a especialidade para o lado servidor, ela vem sem ID
		String titulo = agendamento.getEspecialidade().getTitulo();
		
		//Recupera a especialidade pelo titulo para pegar o ID
		//stream().findFirst() retorna o primeiro elemento da estrutura Set
		Especialidade especialidade = especialidadeService.buscarPorTitulos(new String[] {titulo})
				.stream().findFirst().get();
		
		/*Cria relacionamento de agendamento com especialidade e paciente
		N precisa fazer o msmo para o Medico pq o comboBox q seleciona o Medico está enviando o seu ID,
		então o JPA já consegue fazer o relacionamento dele com o Agendamento. Se tivesse enviando o nome
		e n o ID, precisaria fazer o setMedico*/
		agendamento.setEspecialidade(especialidade);
		agendamento.setPaciente(paciente);
		
		service.salvar(agendamento);
		attr.addFlashAttribute("sucesso", "Sua consulta foi agendada com sucesso.");

		return "redirect:/agendamentos/agendar";		
	}
	
	//Abrir pagina de historico de agendamento do paciente
	@PreAuthorize("hasAnyAuthority('PACIENTE', 'MEDICO')")
	@GetMapping({"/historico/paciente", "/historico/consultas"})
	public String historico() {
		return "agendamento/historico-paciente";
	}
	
	//Localizar o historico de agendamentos por usuario logado
	@PreAuthorize("hasAnyAuthority('PACIENTE', 'MEDICO')")
	@GetMapping("/datatables/server/historico")						//Url q tem na tabela datatable
	public ResponseEntity<?> historicoAgendamentosPorPaciente(HttpServletRequest request, @AuthenticationPrincipal User user) {
		/*HttpServletRequest é um parâmetro necessário para trabalhar com a tabela
		@AuthenticationPrincipal para saber qual usuário está acessando a tabela*/
		
		//O paciente vê todos os dadastros de consultas q ele fez
		//SimpleGrantedAuthority consegue testar um determinado perfil
		if (user.getAuthorities().contains(new SimpleGrantedAuthority(PerfilTipo.PACIENTE.getDesc()))) {
			//O parâmetro ok é o retorno da consulta referente aos dados q quer enviar para a tabela
			return ResponseEntity.ok(service.buscarHistoricoPorPacienteEmail(user.getUsername(), request));
		}
		//O médico vê todas as consultas q foram cadastradas para ele
		if (user.getAuthorities().contains(new SimpleGrantedAuthority(PerfilTipo.MEDICO.getDesc()))) {
			return ResponseEntity.ok(service.buscarHistoricoPorMedicoEmail(user.getUsername(), request));
		}		
		
		return ResponseEntity.notFound().build();					//Envia uma status 404
	}
	
	//Localizar agendamento pelo id e envia-lo para a pagina de cadastro
	@PreAuthorize("hasAnyAuthority('PACIENTE', 'MEDICO')")
	@GetMapping("/editar/consulta/{id}") 
	public String preEditarConsultaPaciente(@PathVariable("id") Long id, 
										    ModelMap model, @AuthenticationPrincipal User user) {
		Agendamento agendamento = service.buscarPorIdEUsuario(id, user.getUsername());
		model.addAttribute("agendamento", agendamento);
		
		return "agendamento/cadastro";
	}
	
	@PreAuthorize("hasAnyAuthority('PACIENTE', 'MEDICO')")
	@PostMapping("/editar")
	public String editarConsulta(Agendamento agendamento, RedirectAttributes attr, @AuthenticationPrincipal User user) {
		String titulo = agendamento.getEspecialidade().getTitulo();
		
		//Pega o ID
		Especialidade especialidade = especialidadeService
				.buscarPorTitulos(new String[] {titulo})
				.stream().findFirst().get();
		
		agendamento.setEspecialidade(especialidade);
		
		service.editar(agendamento, user.getUsername());
		attr.addFlashAttribute("sucesso", "Sua consulta froi alterada com sucesso.");
		
		return "redirect:/agendamentos/agendar";
	}
	
	@PreAuthorize("hasAuthority('PACIENTE')")
	@GetMapping("/excluir/consulta/{id}")
	public String excluirConsulta(@PathVariable("id") Long id, RedirectAttributes attr) {
		service.remover(id);				//Esse ID é do agendamento
		attr.addFlashAttribute("sucesso", "Consulta excluída com sucesso.");
		
		return "redirect:/agendamentos/historico/paciente";
	}
}
