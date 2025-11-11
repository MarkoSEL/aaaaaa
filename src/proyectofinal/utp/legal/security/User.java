package proyectofinal.utp.legal.security;

public class User {
    private final String username;
    private final Role role;
    private final boolean active;

    public User(String username, Role role) { this(username, role, true); }

    public User(String username, Role role, boolean active) {
        this.username = username;
        this.role = role;
        this.active = active;
    }

    public String getUsername() { return username; }
    public Role getRole() { return role; }
    public boolean isActive() { return active; }

    @Override public String toString() {
        return username + " [" + role + "]" + (active ? "" : " (inactivo)");
    }
}
