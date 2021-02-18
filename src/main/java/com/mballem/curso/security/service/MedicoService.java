package com.mballem.curso.security.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mballem.curso.security.domain.Medico;
import com.mballem.curso.security.repository.MedicoRepository;

@Service
public class MedicoService {

	@Autowired
	private MedicoRepository repository;
	
	@Transactional(readOnly = true)
	public Medico buscarPorUsuarioId(Long id) {
		/*Se n existir um medico para o usuario, cria uma instância de Medico. Isso é importante para saber
		se é um insert ou update*/
		return repository.findByUsuarioId(id).orElse(new Medico());
	}

	@Transactional(readOnly = false)
	public void salvar(Medico medico) {
		repository.save(medico);
	}

	/*Obj transiente é aquele q o hibernate n está gerenciando, no caso o medico q recebe no arg
	 *Obj persistente é aquele q está sendo gerenciado
	 O método n precisa trabalhar com o método Save pq a variável m2 está em um estado persistente. Se
	 quiser usar o save tbm n terá problema*/
	@Transactional(readOnly = false)
	public void editar(Medico medico) {
		Medico m2 = repository.findById(medico.getId()).get();		//m2 é um dado persistente
		
		m2.setCrm(medico.getCrm());									//dados obrigatório
		m2.setDtInscricao(medico.getDtInscricao());
		m2.setNome(medico.getNome());
		
		if (!medico.getEspecialidades().isEmpty()) {
			m2.getEspecialidades().addAll(medico.getEspecialidades());
		}
	}
	
	@Transactional(readOnly = true)
	public Medico buscarPorEmail(String email) {
		return repository.findByUsuarioEmail(email).orElse(new Medico());
	}

	@Transactional(readOnly = false)
	public void excluirEspecialidadePorMedico(Long idMed, Long idEsp) {
		Medico medico = repository.findById(idMed).get();		//medico em estado persistente
		//Remove a especialidade da lista se tiver um ID igual ao passado pelo param
		medico.getEspecialidades().removeIf(e -> e.getId().equals(idEsp));
	}
	
	@Transactional(readOnly = true)
	public List<Medico> buscarMedicosPorEspecialidade(String titulo) {
		return repository.findByMedicosPorEspecialidade(titulo);
	}

	@Transactional(readOnly = true)
	public boolean existeEspecialidadeAgendada(Long idMedico, Long idEspecialidade) {
		//isPresent verifica se o obj é null, se for ele retorna false pq n tem nada presente no obj
		return repository.hasEspecialidadeAgendada(idMedico, idEspecialidade).isPresent();
	}
}
