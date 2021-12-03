package br.com.repository;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import br.com.entidades.Estados;
import br.com.entidades.Pessoa;
import br.com.jpautil.JPAUtil;

public class IDaoPessoaImpl implements IDaoPessoa {
	
	@Inject
	private EntityManager entityManager;

	@Override
	public Pessoa consultarUsuario(String login, String senha) {
		EntityManager entityManager = JPAUtil.getEntityManager();
		
		Pessoa pessoa = null;
		
		EntityTransaction entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		
		pessoa = (Pessoa) entityManager.createQuery("select p from Pessoa p where p.login = '" + login + "' and p.senha = '" + senha + "'").getSingleResult();
		
		entityTransaction.commit();
		entityManager.close();
		
		return pessoa;
	}

	@Override
	public List<SelectItem> litaEstados() {
		
		EntityManager entityManager = JPAUtil.getEntityManager();
		List<SelectItem> selectItens = new ArrayList<SelectItem>();
		
		EntityTransaction  entityTransaction = entityManager.getTransaction();
		entityTransaction.begin();
		
		List<Estados> estados = entityManager.createQuery("from Estados").getResultList();
		
		for (Estados estado : estados) {
			selectItens.add(new SelectItem(estado.getId(), estado.getNome()));
			
		}
		
		return selectItens;
		
	}

}
