package br.com.springsecurity.dto;

public record LoginResponse(String acessToken, Long expiresIn) {
}
