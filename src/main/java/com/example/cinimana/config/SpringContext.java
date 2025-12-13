package com.example.cinimana.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Classe utilitaire pour accéder au contexte Spring depuis n'importe où
 * (y compris les EntityListeners JPA)
 */
@Component
public class SpringContext implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    /**
     * Récupère un bean Spring par sa classe
     */
    public static <T> T getBean(Class<T> beanClass) {
        if (context == null) {
            throw new IllegalStateException("Le contexte Spring n'est pas encore initialisé");
        }
        return context.getBean(beanClass);
    }
}