package view;

import viewmodel.ViewModel;

public interface View {
    /**
     * Привязать ViewModel к View.
     */
    void setViewModel(ViewModel vm);

    /**
     * Запустить обработку пользовательского ввода.
     */
    void start();
}
