package com.mballem.curso.security.service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender mailSender;				//Para fazer o envio do email para o servidor de email
	
	@Autowired			//Classe da biblioteca do thymeleaf, é para se comunicar com o template da pag html
	private SpringTemplateEngine template;			
	
	/*destino é o email do usuário q está fazendo cadastrado no sistema
	codigo é um valor q será enviado junto com a url q o usuário vai ter q clicar para q ele volte para a app
	para confirmar a atualização do cadastro. O código é o validador da transação*/
	public void enviarPedidoDeConfirmacaoDeCadastro(String destino, String codigo) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = 
				new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
		
		//Parte referente ao template do thymeleaf					setVariable passa chave e valor
		Context context = new Context();
		context.setVariable("titulo", "Bem vindo a clínica Spring Security");
		context.setVariable("texto", "Precisamos que confirme seu cadastro, clicando no link abaixo");
		/*Envia o link para o usuário fazer a confirmação por meio dele. Se o sistema tiver hospedado em 
		algum servidor, coloca o link do servidor no lugar do localhost*/
		context.setVariable("linkConfirmacao", 
				"http://localhost:8080/u/confirmacao/cadastro?codigo=" + codigo);
		
		/*Esse método acessa a pag de email, pega as os valores da variável context e substitui na pag as 
		 instruções q tem lá*/
		String html = template.process("email/confirmacao", context);
		
		helper.setTo(destino);
		helper.setText(html, true);
		helper.setSubject("Confirmacao de Cadastro");				//Assunto do email
		//Email de qm ta fazendo o envio. Alguns emaisl mostra o original de envio e n o colocado no From
		helper.setFrom("nao-responder@clinica.com.br");				
		
		/*Primeiro param é um ID e o segundo é o caminho de uma img. 
		addInline deve ser a última instrução entre os herlper, se colocar primeiro, pode ter problema de 
		n aparecer a img na pag*/
		helper.addInline("logo", new ClassPathResource("/static/image/spring-security.png"));
		
		mailSender.send(message);
	}
	
	//verificador é o código q o usuário vai precisar digitar no formulário
	public void enviarPedidoRedefinicaoSenha(String destino, String verificador) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = 
        		new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
        
        Context context = new Context();
        context.setVariable("titulo", "Redefinição de Senha");
        context.setVariable("texto", "Para redefinir sua senha use o código de verficação " +
                "quando exigido no formulário." );
        context.setVariable("verificador", verificador);
        
        String html = template.process("email/confirmacao", context);        
        helper.setTo(destino);
        helper.setText(html, true);
        helper.setSubject("Redefinição de Senha");
        helper.setFrom("no-replay@clinica.com.br");

        helper.addInline("logo", new ClassPathResource("/static/image/spring-security.png"));  
       
        mailSender.send(message);		
	}
}
