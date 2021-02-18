//https://jqueryui.com/autocomplete/#multiple-remote
// processa o auto-complete de especialidades
$(function() {
	// remove o espaco depois da virgula
	function split(val) {
		return val.split(/,\s*/);					//Transforma cada posição em uma posição de array
	}

	function extractLast(term) {
		return split(term).pop();					//Remove o ultimo elemento do array e retorna esse elemento
	}
	
	// adicioana a tag de input com a especializacao no html 
	function addEspecializacao(titulo) {				//titulo da especialidade selecionada no componente
		//Adiciona um input no div q tiver o ID "listaEspecializacoes"
		$('#listaEspecializacoes')
			.append('<input type="hidden" value="'+ titulo +'" name="especialidades">');
	}
	
	// mostra na pagina um toast com mensagem de especialidades repetidas 
    function exibeMessagem(texto) {
    	/*Código html do componente toast do bootstrap, esse código tem na pag do bootstrap
    	data-delay="2800" é o tempo q a msg fica na tela*/
        $('.add-toast').append(""
          .concat('<div class="toast" role="alert" aria-live="assertive" aria-atomic="true" data-delay="2800">',
                  '<div class="toast-header">',
                  '<strong class="mr-auto">Atenção</strong>',
                  '<button type="button" class="ml-2 mb-1 close" data-dismiss="toast" aria-label="Close">',
                  '  <span aria-hidden="true">&times;</span>',
                  '</button>',
              '</div>',
              '<div class="toast-body">', texto, '</div>',
          '</div>')
        );
        $('.toast').toast('show');
        $('.toast').on('hidden.bs.toast', function (e) {		
            $(e.currentTarget).remove();			//Remove o componente do Dom para n acumular componentes
        });
    }	

	$("#autocomplete-especialidades")
		.on("keydown",	function(event) {						//keydown captura oq for digitado
			if (event.keyCode === $.ui.keyCode.TAB
					&& $(this).autocomplete("instance").menu.active) {
				event.preventDefault();
			}
		})
		.autocomplete({
			source : function(request, response) {
				$.getJSON("/especialidades/titulo", {			//Url para acessar no lado servidor
	//A var termo q leva oq foi digitado no input. Se digitar "a", envia; se depois digitar "b", envia "a" e "b".
					termo : extractLast(request.term)		//extractLast leva a ult pos doq foi digitado
				}, response);
			},
			search : function() {
				// custom minLength
				var term = extractLast(this.value);
				if (term.length < 1) {						//Qtd de chars para fazer uma busca
					return false;
				}
			},
			focus : function() {
				// prevent value inserted on focus
				return false;
			},
			select : function(event, ui) {
				var terms = split(this.value);		 //this.value é oq tem dentro do componente de input na pag
				console.log('1. this.value: ' + this.value)
				console.log('2. terms: ' + terms)
				//ui.item.value é o valor do item selecionado no comboBox, se escolher "abc", mostrada "abc"
				console.log('3. ui.item.value: ' + ui.item.value)
				// remove a entrada atual
				terms.pop();
				console.log('4. terms: ' + terms)
				// testa se valor já foi inserido no array
				var exists = terms.includes(ui.item.value);				
				if (exists === false) {				
					// add the selected item
					terms.push(ui.item.value);
					console.log('5. terms: ' + terms)
					terms.push("");							//Cria uma pos (str vazia) dentro do array
					console.log('6. terms: ' + terms)
					this.value = terms.join(", ");		   //Adiciona uma ", " na ultima pos no lugar da str vazia
					console.log('7. this.value: ' + this.value)
					console.log('8. ui.item.value: ' + ui.item.value)
					// adiciona especializacao na pagina para envio ao controller
					addEspecializacao(ui.item.value);
				} else {
					exibeMessagem('A Especialização <b>'+ ui.item.value +'</b> já foi selecionada.');
				}
				return false;
			}
		});
});
