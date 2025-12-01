package in.tech_camp.pictweet.system;//ツイート削除の結合テスト

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import in.tech_camp.pictweet.PictweetApplication;
import in.tech_camp.pictweet.entity.UserEntity;
import in.tech_camp.pictweet.entity.TweetEntity;
import in.tech_camp.pictweet.factory.UserFormFactory;
import in.tech_camp.pictweet.factory.TweetFormFactory;
import in.tech_camp.pictweet.form.UserForm;
import in.tech_camp.pictweet.form.TweetForm;
import in.tech_camp.pictweet.service.UserService;
import in.tech_camp.pictweet.repository.TweetRepository;
import static in.tech_camp.pictweet.support.LoginSupport.login;

@ActiveProfiles("test")
@SpringBootTest(classes = PictweetApplication.class)
@AutoConfigureMockMvc
public class TweetDeleteIntegrationTest {
  private UserForm userForm1;
  private UserEntity userEntity1;

  private UserForm userForm2;
  private UserEntity userEntity2;

  private TweetForm tweetForm1;
  private TweetEntity tweetEntity1;

  private TweetForm tweetForm2;
  private TweetEntity tweetEntity2;

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
  userForm1 = UserFormFactory.createUser();
  userEntity1 = new UserEntity();
  userEntity1.setEmail(userForm1.getEmail());
  userEntity1.setnickname(userForm1.getNickname());
  userEntity1.setPassword(userForm1.getPassword());
  userService.createUserWitEncryptedPassword(userEntity1);

  userForm2 = UserFormFactory.createUser();
  userEntity2 = new UserEntity();
  userEntity2.setEmail(userForm2.getEmail());
  userEntity2.setnickname(userForm2.getNickname());
  userEntity2.setPassword(userForm2.getPassword());
  userService.createUserWitEncryptedPassword(userEntity2);

  tweetForm1 = TweetFormFactory.createTweet();
  tweetEntity1 = new TweetEntity();
  tweetEntity1.setUser(userEntity1);
  tweetEntity1.setImage(tweetForm1.getImage());
  tweetEntity1.setText(tweetForm1.getText());
  tweetRepository.insert(tweetEntity1);

  tweetForm2 = TweetFormFactory.createTweet();
  tweetEntity2 = new TweetEntity();
  tweetEntity2.setUser(userEntity2);
  tweetEntity2.setImage(tweetForm2.getImage());
  tweetEntity2.setText(tweetForm2.getText());
  tweetRepository.insert(tweetEntity2);
 }

  @Nested
  class ツイートが削除できるとき {
    @Test
    public void ログインしたユーザーは自らが投稿したツイートの削除ができる() throws Exception {
      // ツイート1を投稿したユーザーでログインする
      MvcResult loginResult = mockMvc.perform(post("/login")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", userForm1.getEmail())
        .param("password", userForm1.getPassword())
        .with(csrf()))
        .andReturn();

      MockHttpSession session = login(mockMvc, userForm1);
      assertNotNull(session); 

      // ツイート1に「削除」へのリンクがあることを確認する
      MvcResult pageResult = mockMvc.perform(get("/").session(session))
          .andReturn();
      String topPageContent = pageResult.getResponse().getContentAsString();
      Document topPageDocument = Jsoup.parse(topPageContent);
      Element deleteMenuElement = topPageDocument.selectFirst("form[action='/tweets/" + tweetEntity1.getId() + "/delete']");
      assertNotNull(deleteMenuElement);
      Element deleteButtonElement = deleteMenuElement.selectFirst("input[type='submit']");
      assertNotNull(deleteButtonElement);
      assertEquals("削除", deleteButtonElement.val());

      List<TweetEntity> tweetsListBeforeDeletion = tweetRepository.findAll();
      initialCount = tweetsListBeforeDeletion.size();

      // 投稿を削除する
      mockMvc.perform(post("/tweets/{tweetId}/delete",tweetEntity1.getId()).session(session)
          .with(csrf()))
          .andExpect(status().isFound())
          .andExpect(redirectedUrl("/"));

      // 投稿を削除するとレコードの数が1減ることを確認する
      List<TweetEntity> tweetsListAfterDeletion = tweetRepository.findAll();
      afterCount = tweetsListAfterDeletion.size();
      assertEquals(initialCount - 1, afterCount);

      // トップページにはツイート1の内容が存在しないことを確認する（画像）
      MvcResult pageResultAfterEdit = mockMvc.perform(get("/"))
          .andReturn();
      String pageContentAfterEdit = pageResultAfterEdit.getResponse().getContentAsString();
      Document documentAfterEdit = Jsoup.parse(pageContentAfterEdit);
      Element divElement = documentAfterEdit.selectFirst(".content_post[style='background-image: url(" + tweetForm1.getImage() + ");']");
      assertNull(divElement);

      // トップページにはツイート1の内容が存在しないことを確認する（テキスト）
      mockMvc.perform(get("/"))
          .andExpect(content().string(not(containsString(tweetEntity1.getText()))));

    }
  }




    @Nested
  class ツイート削除ができないとき {
    @Test
    public void ログインしたユーザーは自分以外が投稿したツイートの削除ができない() throws Exception {
      // ツイート1を投稿したユーザーでログインする
      MvcResult loginResult = mockMvc.perform(post("/login")
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .param("email", userForm1.getEmail())
          .param("password", userForm1.getPassword())
          .with(csrf()))
          .andReturn();

      MockHttpSession session = login(mockMvc, userForm1);
      assertNotNull(session);

      // ツイート2に「削除」へのリンクがないことを確認する
      MvcResult pageResult = mockMvc.perform(get("/").session(session))
          .andReturn();
      String pageContent = pageResult.getResponse().getContentAsString();
      Document document = Jsoup.parse(pageContent);
      Element deleteMenuElement = document.selectFirst("form[action='/tweets/" + tweetEntity2.getId() + "/delete']");
      assertNull(deleteMenuElement);
     }

    @Test
    public void ログインしていないとツイートの削除ができない() throws Exception {
      // ログインせずにトップページにアクセス
      MvcResult pageResult = mockMvc.perform(get("/"))
      .andReturn();
      String pageContent = pageResult.getResponse().getContentAsString();
      Document document = Jsoup.parse(pageContent);

      // ツイート1に「削除」へのリンクがないことを確認する
      Element tweet1DeleteMenuElement = document.selectFirst("form[action='/tweets/" + tweetEntity1.getId() + "/delete']");
      assertNull(tweet1DeleteMenuElement);
      // ツイート2に「削除」へのリンクがないことを確認する
      Element tweet2DeleteMenuElement = document.selectFirst("form[action='/tweets/" + tweetEntity2.getId() + "/delete']");
      assertNull(tweet2DeleteMenuElement);
    }
  }

}

 