package com.mballem.curso.security.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.mballem.curso.security.domain.Especialidade;

public interface EspecialidadeRepository extends JpaRepository<Especialidade, Long>{

	//% para pesquisar de acordo com oq o usuário for digitando, o trecho da palavra consultada
	@Query("select e from Especialidade e where e.titulo like :search%")
	Page<Especialidade> findAllByTitulo(String search, Pageable pageable);
	
	@Query("select e.titulo from Especialidade e where e.titulo like :termo%")
	List<String> findEspecialidadesByTermo(String termo);

	//Pesquisa cada titulo digitado no campo de input. Como vai testar um array, usa o IN
	@Query("select e from Especialidade e where e.titulo IN :titulos")
	Set<Especialidade> findByTitulos(String[] titulos);

	@Query("select e from Especialidade e "
			+ "join e.medicos m "
			+ "where m.id = :id") 
	Page<Especialidade> findByIdMedico(Long id, Pageable pageable);
}
