package com.mballem.curso.security.web.conversor;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.mballem.curso.security.domain.Especialidade;
import com.mballem.curso.security.service.EspecialidadeService;

/*Quando seleciona uma lista de especialidades para o médico, envia para o servidor um str[]0
 *A saída é Set<Especialidade> pq Medico tem o tipo Set<Especialidade> e n List<Especialidade>*/
@Component
public class EspecialidadesConverter implements Converter<String[], Set<Especialidade>> {

	@Autowired
	private EspecialidadeService service;

	@Override
	public Set<Especialidade> convert(String[] titulos) {
		Set<Especialidade> especialidades = new HashSet<>();
		
		if (titulos != null && titulos.length > 0) {
			especialidades.addAll(service.buscarPorTitulos(titulos));			
		}
		return especialidades;
	}
}
