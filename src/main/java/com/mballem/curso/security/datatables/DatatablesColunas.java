package com.mballem.curso.security.datatables;

//Atributos de colunas para cada tipo de tabela. Passa a variável de acordo com a tabela desejada
public class DatatablesColunas {

	public static final String[] ESPECIALIDADES = {"id", "titulo"};

	public static final String[] MEDICOS = {"id", "nome", "crm", "dtInscricao", "especialidades"};
	
	public static final String[] AGENDAMENTOS = {"id", "paciente.nome", "dataConsulta", "medico.nome", "especialidade.titulo"};

	public static final String[] USUARIOS = {"id", "email", "ativo", "perfis"};	
}
