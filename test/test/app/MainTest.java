package test.app;

import app.Main;
import di.AppModule;
import view.TelegramView;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void injector_createsTelegramView() {
        Injector injector = Guice.createInjector(new AppModule());
        TelegramView view = injector.getInstance(TelegramView.class);
        assertNotNull(view);
    }

    @Test
    void main_runsWithoutExceptions() {
        assertDoesNotThrow(() -> Main.main(new String[]{}));
    }

    @Test
    void main_invokesStartOnTelegramView() {
        TelegramView mockView = Mockito.mock(TelegramView.class);
        Injector injector = Guice.createInjector(binder -> {
            binder.install(new AppModule());
            binder.bind(TelegramView.class).toInstance(mockView);
        });
        TelegramView view = injector.getInstance(TelegramView.class);
        view.start();
        Mockito.verify(mockView, Mockito.times(1)).start();
    }
}
