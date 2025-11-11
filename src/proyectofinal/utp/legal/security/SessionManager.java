package proyectofinal.utp.legal.security;

import proyectofinal.utp.legal.facade.DocumentUseCases;

public final class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();
    private User current;

    private SessionManager(){}

    public static SessionManager get(){ return INSTANCE; }

    public void login(User u){ this.current = u; }
    public void logout(){ this.current = null; }
    public boolean isLogged(){ return current != null; }
    public User currentUser(){ return current; }

    public DocumentUseCases securedService() {
        if (current == null) throw new IllegalStateException("sin sesion");
        return new SecuredDocumentServiceProxy(current); // usa tu ctor existente (User)
    }
}
