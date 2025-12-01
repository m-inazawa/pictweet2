package in.tech_camp.pictweet.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import in.tech_camp.pictweet.PictweetApplication;
import in.tech_camp.pictweet.entity.UserEntity;
import in.tech_camp.pictweet.factory.UserFormFactory;
import in.tech_camp.pictweet.form.UserForm;
import in.tech_camp.pictweet.service.UserService;


@ActiveProfiles("test")
@SpringBootTest(classes = PictweetApplication.class)
@AutoConfigureMockMvc
public class UserLoginIntegrationTest {
  @Autowired//必要なクラスのインスタンスを自動生成
  private MockMvc mockMvc;

  @Autowired
  private UserService userService;

  private UserForm userForm;

  @BeforeEach
  public void setup() {
    userForm = UserFormFactory.createUser();
    UserEntity userEntity = new UserEntity();
    userEntity.setEmail(userForm.getEmail());
    userEntity.setNickname(userForm.getNickname());
    userEntity.setPassword(userForm.getPassword());

    userService.createUserWithEncryptedPassword(userEntity);
    //UserEntityクラスにユーザー登録に必要な情報を設定
    //UserServiceクラスにUserEntityクラスの情報を渡す
    //テスト用のデータベース(H2データベース)に新しいユーザー情報を作成
  }

  @Nested
  class ユーザーログインができるとき {
    @Test
    public void 保存されているユーザーの情報を合致すればログインができる() throws Exception {
      // トップページに移動する
      mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(view().name("tweets/index"))
      // トップページにログインページへ遷移するボタンがあることを確認する
        .andExpect(content().string(org.hamcrest.Matchers.containsString("ログイン")));

      // ログインページに遷移する
      mockMvc.perform(get("/users/login"))
        .andExpect(status().isOk())
        .andExpect(view().name("users/login"));

      // 正しいユーザー情報を入力してログインを試みる
      MvcResult loginResult = mockMvc.perform(post("/login")//MvcResult：MockMvcテストで各リクエストの結果を保持するオブジェクト
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", userForm.getEmail())
        .param("password", userForm.getPassword())
        .with(csrf()))
        .andExpect(status().isFound())
        .andExpect(redirectedUrl("/"))
        .andReturn();

      MockHttpSession session = (MockHttpSession)loginResult.getRequest().getSession();
      // MockHttpSession：テスト中のセッション情報を再現するためのオブジェクト
      //top pageに遷移することを確認する
      mockMvc.perform(get("/").session(session))//ウェブサイトでユーザーの情報を一時的に記録する仕組み
        .andExpect(status().isOk())
        .andExpect(view().name("tweets/index"))
      // ログアウトボタンが表示されることを確認する
      .andExpect(content().string(org.hamcrest.Matchers.containsString("logout-btn")))
      // 新規登録ページへ遷移するボタンやログインページへ遷移するボタンが表示されていないことを確認
      .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("新規登録"))));

    }
  }


  @Nested
  class ユーザーログインができないとき{
    @Test
    public void 保存されているユーザーの情報と合致しないとログインができない() throws Exception {
      // トップページに移動する
      mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(view().name("tweets/index"))
        // トップページにログインページへ遷移するボタンがあることを確認する
        .andExpect(content().string(org.hamcrest.Matchers.containsString("ログイン")));
      // ログインページに遷移する
      mockMvc.perform(get("/users/login"))
        .andExpect(status().isOk())
        .andExpect(view().name("users/login"));

      // 間違ったユーザー情報でログインを試みる
      mockMvc.perform(post("/login")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("username", "test")
        .param("password", "")
        .with(csrf()))
      .andExpect(redirectedUrl("/login?error"))
      .andExpect(status().isFound());    }
    
  }

}
