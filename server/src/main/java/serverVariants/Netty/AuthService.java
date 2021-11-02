package serverVariants.Netty;

public interface AuthService <T> extends CrudService<T, Long> {
    String findByLogin(String login);
}
