package com.mballem.curso.security.web.exception;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import com.mballem.curso.security.exception.AcessoNegadoException;

/*Essa anotação serve como se fosse um ouvinte na aplicação
 Algumas regras são criadas nessa classe e a anotação fica esperando q algumas dessas regras sejam 
 verdadeiras Quando forem verdadeiras, a classe ativa o método referente à regra
*/
@ControllerAdvice					
public class ExceptionController {

	/*Essa anotação recebe como parâmetro a exceção q deseja tratar. Td vez q ser lançado uma exceção
	do tipo UsernameNotFoundException, entrará nesse método e o Model irá retornar para a pag especificada*/
	@ExceptionHandler(UsernameNotFoundException.class)			
	public ModelAndView usuarioNaoEncontradoException(UsernameNotFoundException ex) {
		ModelAndView model = new ModelAndView("error");
		model.addObject("status", 404);
		model.addObject("error", "Operação não pode ser realizada.");
		model.addObject("message", ex.getMessage());
		return model;
	}
	
	//Protege de usuário q digitar valor de ID de outro usuário e de NullPontException (id q n existe)
	@ExceptionHandler(AcessoNegadoException.class)			
	public ModelAndView acessoNegadoException(AcessoNegadoException ex) {
		ModelAndView model = new ModelAndView("error");
		model.addObject("status", 403);							//status 403 - acesso negado
		model.addObject("error", "Operação não pode ser realizada.");
		model.addObject("message", ex.getMessage());
		return model;
	}
}
