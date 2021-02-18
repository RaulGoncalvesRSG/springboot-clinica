package com.mballem.curso.security.exception;

@SuppressWarnings("serial")
public class AcessoNegadoException extends RuntimeException {

	//RuntimeException é uma exceção padrão do Java para q seja lançada em tempo de execução
	public AcessoNegadoException(String message) {
		super(message);
	}
}
