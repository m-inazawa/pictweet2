package in.tech_camp.pictweet.system;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import static in.tech_camp.pictweet.support.LoginSupport.login;

import in.tech_camp.pictweet.PictweetApplication;
import in.tech_camp.pictweet.entity.TweetEntity;
import in.tech_camp.pictweet.entity.UserEntity;
import in.tech_camp.pictweet.factory.TweetFormFactory;
import in.tech_camp.pictweet.factory.UserFormFactory;
import in.tech_camp.pictweet.form.TweetForm;
import in.tech_camp.pictweet.form.UserForm;
import in.tech_camp.pictweet.repository.TweetRepository;
import in.tech_camp.pictweet.service.UserService;

@ActiveProfiles("test")
@SpringBootTest(classes = PictweetApplication.class)
@AutoConfigureMockMvc
public class TweetPostIntegrationTest {

  private UserForm userForm;
  private UserEntity userEntity;

  private TweetForm tweetForm;

  @Autowired
  private UserService userService;

  @Autowired
  private TweetRepository tweetRepository;

  @Autowired
  private MockMvc mockMvc;

  private int initialCount;
  private int afterCount;

  @BeforeEach
  public void setup() {
    userForm = UserFormFactory.createUser();
    userEntity = new UserEntity();
    userEntity.setEmail(userForm.getEmail());
    userEntity.setNickname(userForm.getNickname());
    userEntity.setPassword(userForm.getPassword());
    userService.createUserWithEncryptedPassword(userEntity);

    tweetForm = TweetFormFactory.createTweet();
  }

  @Nested
  class ツイート投稿ができるとき {
    @Test
    public void ログインしたユーザーは新規投稿できる() throws Exception {
      // ログインする
      MvcResult loginResult = mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("email", userForm.getEmail())
            .param("password", userForm.getPassword())
            .with(csrf()))
            .andReturn();

      MockHttpSession session = login(mockMvc, userForm);            
      // MockHttpSession session  = (MockHttpSession)loginResult.getRequest().getSession();
      assertNotNull(session);

      // 新規投稿ページへのボタンがあることを確認する
      mockMvc.perform(get("/").session(session))
          .andExpect(status().isOk())
          .andExpect(content().string(containsString("投稿する"))); 

      // 投稿ページに移動する
      mockMvc.perform(get("/tweets/new").session(session))
          .andExpect(status().isOk())
          .andExpect(view().name("tweets/new"));

      List<TweetEntity> tweetsListBeforePost = tweetRepository.findAll();
      initialCount = tweetsListBeforePost.size();

       // フォームに情報を入力する
      mockMvc.perform(post("/tweets").session(session)
          .param("text", tweetForm.getText())
          .param("image", tweetForm.getImage())
          .with(csrf()))
          .andExpect(status().isFound())
          .andExpect(redirectedUrl("/"));

      // 送信するとTweetテーブルのレコード数が1上がることを確認する
      List<TweetEntity> tweetsListAfterPost = tweetRepository.findAll();
      afterCount = tweetsListAfterPost.size();
      assertEquals(initialCount + 1, afterCount);

      // トップページには先ほど投稿した内容のツイートが存在することを確認する（画像）
      MvcResult pageResult = mockMvc.perform(get("/"))
          .andReturn();

      String content = pageResult.getResponse().getContentAsString();
      Document document = Jsoup.parse(content);
      Element divElement = document.selectFirst(".content_post[style='background-image: url(" + tweetForm.getImage() + ");']");
      assertNotNull(divElement);

      // トップページには先ほど投稿した内容のツイートが存在することを確認する（テキスト）
      mockMvc.perform(get("/"))
          .andExpect(status().isOk())
          .andExpect(content().string(containsString(tweetForm.getText())));    
    }
  }

  // ①mockMvcを使用してレスポンスをMvcResultで受け取る
  // ②レスポンスの情報は特殊な構造になっているため、getContentAsStringで文字列に変換

  @Nested
  class ツイート投稿ができないとき {
    @Test
    public void ログインしていないと新規投稿ページに遷移できない() throws Exception {
      // トップページに新規投稿ページへのボタンがないことを確認する
      mockMvc.perform(get("/"))
          .andExpect(status().isOk())
          .andExpect(content().string(not(containsString("投稿する"))));
     }
  }
}






