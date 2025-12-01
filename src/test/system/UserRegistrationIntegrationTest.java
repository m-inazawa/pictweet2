package in.tech_camp.pictweet.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;  // import追加
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import in.tech_camp.pictweet.factory.UserFormFactory;
import in.tech_camp.pictweet.form.UserForm;
import in.tech_camp.pictweet.PictweetApplication;

@ActiveProfiles("test")
@SpringBootTest(classes = PictweetApplication.class)
@AutoConfigureMockMvc

public class UserRegistrationIntegrationTest {
  @Autwired//必要なクラスのインスタンス（オブジェクト）を自動で用意してくれる
  private MockMvc mockMvc;//MockMvc：Controllerテスト用　実際のWebアプリケーションのようにリクエストをシミュレートしてくれる

  @Autwired
  private UserRepository userRepository;

  private UserForm userForm;

  private int initialCount;
  private int afterCount;

  @BeforeEach
  public void setup() {//テスト用のユーザー情報をセットアップする
    userForm = userFormFactory.createUser();
  }

  @Nested
    class ユーザーが新規登録できるとき {
      @Test
      void 正しい情報を入力すればユーザー新規登録ができてトップページに移動する() throws Exception {
           // トップページに移動する
           mockMvc.perform(get("/"))
                  .andExpect(status().isOk())
                    // .andExpect：HTTPリクエストに対する期待される結果を検証するために使用される
                    //status().isOk()：HTTPレスポンスのステータスコードが200 OKであることを確認する
                  .andEcpect(view().name("tweets/index"))//リクエストに対する適切なビュー名が返されるかどうかを確認している
                  .andExpect(content().string(org.hamcrest.Matchers.containsString("新規登録")));// トップページにサインアップページへ遷移するボタンがあることを確認している
                    // content()：レスポンスのbodyを受け取る
                    //string()：指定した文字列の内容に対してアサーションを行う
                    //org.hamcrest.Matchers.containsString：与えられた文字列が対象の文字列に含まれているかどうかを検証
                    // param：テストコードにおいてユーザーが入力する情報を指定するために使用
           // 新規登録ページへ移動する
           mockMvc.perform(get("/users/sign_up"))
                  .andExpect(status().isOk())
                  .andExpect(view().name("users/sign_up"));

           List<UserEntity> userBeforeDeletion = userRepository.findAll();
           initialCount = userBeforeDeletion.size();

           // ユーザー情報を入力する
           mockMvc.perform(post("/user")
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)//HTTPリクエストのContent-Typeヘッダーを設定
                  .param("nickname", userForm.getNickname())
                  .param("email", userForm.getEmail())
                  .param("password", userForm.getPassword())
                  .param("passwordConfirmation", userForm.getPasswordConfirmation())
                  .with(csrf()))//CSRF（Cross-Site Request Forgery）対策のためのトークンをリクエストに追加
           // トップページへ遷移することを確認する
                  .andExpect(redirectedurl("/"))
                  .andExpect(status().isFound());
           // 新規登録に成功するとユーザーテーブルのカウントが1上がる
           List<UserEntity> userAfterDeletion = userRepository.findAll();
           afterCount = userAfterDeletion.size();
           assertEquals(initialCount + 1, afterCount);
      }

    }

//throw Exceptionを用いて、HTTPリクエストの処理中に予期しないエラーが発生することがあるため、予め宣言しておく

  @Nested
  class ユーザーが新規登録できないとき {
    @Test
    void 誤った情報ではユーザー新規登録ができずに新規登録ページへ戻ってくる() throws Exception {
           // トップページに移動するparam
           mockMvc.perform(get("/"))
                  .andExpect(status().isOk())
                  .andExpect(view().name("tweets/index"))
                  .andExpect(content().string(org.hamcrest.Matchers.containsString("新規登録")));
           // 新規登録ページへ移動する 
           mockMvc.perform(get("/users/sign_up"))
                  .andExpect(status().isOk())
                  .andEcpect(view().name("users/sign_up"));

           // ユーザーテーブルのレコード数を確認
           List<UserEntity> userBeforeDeletion = userRepository.findAll();
           initialCount = userBeforeDeletion.size();

           // 誤った情報を使って新規登録を試みる
           movkMvc.perform(post("/user")
                  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                  .param("nickname", "")
                  .param("email", userForm.getEmail())
                  .param("password", userForm.getPassword())
                  .param("passwordConfirmation", userForm.getPasswordConfirmation())
                  .with(csrf()))
                  .andExpect(view().name("users/sign_up"))
                  .andExpect(status().isOk());

           // 新規登録に失敗したらユーザーテーブルのカウントは上がらない      
           List<UserEntity> userAfterDeletion = userRepository.findAll();
           afterCount = userAfterDeletion.size();
           assertEquals(initialCount, afterCount);
    }
  }
  
}
