package app;

import di.AppModule;
import view.*;


import com.google.inject.Guice;
import com.google.inject.Injector;

public class Main {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new AppModule());
        TelegramView view = injector.getInstance(TelegramView.class);
        view.start();
    }
}

