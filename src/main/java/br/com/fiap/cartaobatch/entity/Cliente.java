package br.com.fiap.cartaobatch.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class Cliente {
	
    private String nome;
    private int matricula;
    private String matriculaTxt;
    private String turma;
    private double limite;

}
