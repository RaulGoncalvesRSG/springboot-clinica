package com.mballem.curso.security.web.conversor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.mballem.curso.security.domain.Perfil;

/*Esta classe é para o comboBox com multipla opção na pag de cadastro de usuario, no qual mais de um obj 
 pode ser escolhido. Ao escolher uma opção, é enviado o ID em vez do obj e esse id chega com string.
 *String[] pq cada id selecionado na pag vai chegar aq como valor do tipo string.
 
 Quando apenas uma opção é selecionada, chega com uma string e n um array, então poderia criar uma outra
 classe q espera apenas um obj str ou então fazer modificação no thymeleaf*/
@Component		
public class PerfisConverter implements Converter<String[], List<Perfil>>{

	@Override
	public List<Perfil> convert(String[] source) {
		List<Perfil> perfis = new ArrayList<>();
		
		for (String id : source) {
			if (!id.equals("0")) {					//Se for 0, apenas um perfil foi selecionado
				perfis.add(new Perfil(Long.parseLong(id)));		//Converte o id (string) para long
			}
		}
		return perfis;
	}
}
