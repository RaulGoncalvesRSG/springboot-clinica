package com.mballem.curso.security.repository.projection;

import com.mballem.curso.security.domain.Especialidade;
import com.mballem.curso.security.domain.Medico;
import com.mballem.curso.security.domain.Paciente;

/*Adiciona os campos e os tipos de retorno q deseja projetar para o retorno de uma consulta
N usa o atributo e sim o método get + nome atributo

Parece com o DTO, mas dessa forma cria uma interface e não uma classe e os retornos são definidos a
partir de método get com o tipo referente ao retorno*/
public interface HistoricoPaciente {

	Long getId();
	
	Paciente getPaciente();
	
	String getDataConsulta();
	
	Medico getMedico();
	
	Especialidade getEspecialidade();
}
