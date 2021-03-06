package com.example.algamoneyapi.resource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.algamoneyapi.event.RecursoCriadoEvent;
import com.example.algamoneyapi.exceptionHandler.AlgamoneyExceptionHandler.Erro;
import com.example.algamoneyapi.model.Lancamento;
import com.example.algamoneyapi.model.Pessoa;
import com.example.algamoneyapi.repository.LancamentoRepository;
import com.example.algamoneyapi.repository.filter.LancamentoFilter;
import com.example.algamoneyapi.service.LancamentoService;
import com.example.algamoneyapi.service.exception.PessoaInexistenteOuInativoException;

@RestController
@RequestMapping("/lancamentos")
public class LancamentoResource {

	@Autowired 
	private LancamentoRepository lancamentoRepository;
	
	@Autowired
	private ApplicationEventPublisher publisher;
	
	@Autowired 
	private LancamentoService lancamentoService;
	
	@Autowired
	private MessageSource  messageSource;
	
	@GetMapping
	public Page<Lancamento> pesquisar (LancamentoFilter lancamentoFilter, Pageable pageable){
		return lancamentoRepository.filtrar(lancamentoFilter, pageable);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Lancamento> buscarPeloCodigo(@PathVariable Long id) {
		 Optional<Lancamento> pessoa = lancamentoRepository.findById(id);
		
		if(pessoa.isPresent()) {
			return ResponseEntity.ok(pessoa.get());
		}
			return ResponseEntity.notFound().build();
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<Lancamento> Criar(@Valid @RequestBody Lancamento lancamento, HttpServletResponse response) throws PessoaInexistenteOuInativoException {
		Lancamento lancamentoSalvar = lancamentoService.salvar(lancamento);
		
		publisher.publishEvent(new RecursoCriadoEvent(this, response, lancamentoSalvar.getCodigo()));
		
		return ResponseEntity.status(HttpStatus.CREATED).body(lancamentoSalvar);
	}
	
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remover(@PathVariable Long id) {
		
		lancamentoRepository.deleteById(id);
		
	}
	
	@ExceptionHandler({PessoaInexistenteOuInativoException.class})
	public ResponseEntity<Object> handlePessoaInexistenteOuInativoException(PessoaInexistenteOuInativoException ex){
		String mensagemUsuario = messageSource.getMessage("pessoa.inexistente-ou-inativa", null, LocaleContextHolder.getLocale());
		String mensagemDesenvolvedor = ex.toString();
		List<Erro> erros = Arrays.asList(new Erro(mensagemUsuario, mensagemDesenvolvedor));
		
		return ResponseEntity.badRequest().body(erros);
		}
	
	
}
