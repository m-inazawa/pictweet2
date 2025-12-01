package in.tech_camp.pictweet;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                .requestMatchers("/css/**", "/images/**", "/", "/users/sign_up", "/users/login", "/tweets/{id:[0-9]+}","/users/{id:[0-9]+}","/tweets/search").permitAll()
                            // requestMatchers（）で指定したパスがログアウト状態でも表示できるページとなる
                            // そのほかのページは、SpringSecurityによりログインページにリダイレクトされる
                            //ここに記述されたGETリクエストは未ログインでも憑依を許可
                            .requestMatchers(HttpMethod.POST, "/user").permitAll()
                            //ここに記述されたPOSTリクエストは未ログインでも表示を許可
// .requestMatchers(HttpMethod.POST, "/user").permitAll()は、POSTのリクエストを対象にしている。
// HttpMethodの記述がないものはデフォルトでGETのリクエストが対象となる
// 「正規表現」
                            .anyRequest().authenticated())
                            //上記以外のリクエストはログインユーザーのみ認証され表示される

                .formLogin(login -> login
                        .loginProcessingUrl("/login")//ログインフォームでログインボタンを押した際のパスを設定
                        .loginPage("/users/login") //ログインフォームのパスを設定
                        .defaultSuccessUrl("/", true)//ログイン成功後のリダイレクト先を設定
                        .failureUrl("/login?error")// ログイン失敗後のリダイレクト先を設定
                        // ↑リクエストを送る際に、errorという名前のパラメーターで、エラー情報も含めてリクエストがコントローラーに送られる
                        // コントローラーは、@RequestParamで受ける
                        .usernameParameter("email") //ログイン時にusernameとして扱うパラメーターを指定
                        .permitAll())

                .logout(logout -> logout
                        .logoutUrl("/logout")//ログアウトボタンを押した際のパスを設定
                        .logoutSuccessUrl("/"));//ログアウト成功時のリダイレクト先

        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}