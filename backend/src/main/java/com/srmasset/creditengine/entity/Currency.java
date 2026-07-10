package com.srmasset.creditengine.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * Representa uma moeda suportada pela plataforma (ex: BRL, USD).
 * Usamos o codigo ISO 4217 como chave primaria natural, ja que
 * moedas sao um conjunto pequeno e estavel de valores.
 */
@Entity
@Table(name = "currency")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Currency implements Serializable {

    @Id
    @Column(name = "code", length = 3, nullable = false)
    private String code; // ISO 4217, ex: BRL, USD

    @Column(name = "name", nullable = false, length = 60)
    private String name;

    @Column(name = "symbol", length = 5)
    private String symbol;
}
