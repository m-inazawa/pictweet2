package in.tech_camp.pictweet.system;

import org.apache.tomcat.util.http.parser.MediaType;
import org.attoparser.dom.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import in.tech_camp.pictweet.PictweetApplication;
import in.tech_camp.pictweet.entity.UserEntity;
import in.tech_camp.pictweet.entity.TweetEntity;
import in.tech_camp.pictweet.factory.UserFormFactory;
import in.tech_camp.pictweet.factory.TweetFormFactory;
import in.tech_camp.pictweet.form.UserForm;
import in.tech_camp.pictweet.form.TweetForm;
import in.tech_camp.pictweet.service.UserService;
import static in.tech_camp.pictweet.support.LoginSupport.login;
import in.tech_camp.pictweet.repository.TweetRepository;

@ActiveProfiles("test")
@SpringBootTest(classes = PictweetApplication.class)
@AutoConfigureMockMvc
public class TweetDetailIntegrationTest {
  private UserForm userForm;
  private UserEntity userEntity;

  private TweetForm tweetForm;
  private TweetEntity tweetEntity;

  @Autowired
  private UserService userService;

  @Autowired
  private TweetRepository tweetRepository;

  @BeforeEach
  public void setup() {
    userForm = UserFormFactory.createUser();
    userEntity = new UserEntity();
    userEntity.setEmail(userForm.getEmail());
    userEntity.setNickname(userForm.getNickname());
    userEntity.setPassword(userForm.getPassword());
    userService.createUserWithEncryptedPassword(userEntity);

    tweetForm = TweetFormFactory.createTweet();
    tweetEntity = new TweetEntity();
    tweetEntity.setUser(userEntity);
    tweetEntity.setImage(tweetForm.getImage());
    tweetEntity.setText(tweetForm.getText());
    tweetRepository.insert(tweetEntity);
  }

  @Test
  public void ログインしたユーザーはツイート詳細ページに遷移してコメント投稿欄が表示される() throws Exception {
    // ログインする
    MvcResult loginResult = mockMvc.perform(post("/login")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", userForm.getEmail())
        .param("password", userForm.getPassword())
        .with(csrf()))
        .andReturn();

    MockHttpSession session = login(mockMvc, userForm);
    assertNotNull(session);

    // ツイートに「詳細」へのリンクがあることを確認する
    MvcResult pageResult = mockMvc.perform(get("/").session(session))
      .andReturn();
    String topPageContent = pageResult.getResponse().getContentAsString();
    Document topPageDocument = Jsoup.parse(topPageContent);
    Element detailMenuElement = topPageDocument.selectFirst("a[href='/tweets/" + tweetEntity.getId() + "']");
    assertNotNull(detailMenuElement);
    assertEquals("詳細", detailMenuElement.text());

    // 詳細ページsrc/testに遷移すると、ツイートの内容が表示されていることを確認する（画像）
    MvcResult detailPageResult = mockMvc.perform(get("/tweets/{tweetId}" ,tweetEntity.getId()).session(session))
        .andExpect(status().isOk())
        .andExpect(view().name("tweets/detail"))
        .andReturn();
    String detailPageContent = detailPageResult.getResponse().getContentAsString();
    Document detailPageDocument = Jsoup.parse(detailPageContent);
    Element divElement = detailPageDocument.selectFirst(".content_post[style='background-image: url(" + tweetEntity.getImage() + ");']");
    assertNotNull(divElement);

    // 詳細ページに遷移すると、ツイートの内容が表示されていることを確認する（テキスト）
    mockMvc.perform(get("/tweets/{tweetId}" ,tweetEntity.getId()).session(session))
        .andExpect(content().string(containsString(tweetEntity.getText())));

    // コメント用のフォームが存在する
    Element commentFormElement = detailPageDocument.selectFirst("form");
    assertNotNull(commentFormElement);

  }


 @Test
 public void ログインしていない状態でツイート詳細ページに遷移できるもののコメント投稿欄が表示されない() throws Exception {
    // ツイートに「詳細」へのリンクがあることを確認する
    MvcResult pageResult = mockMvc.perform(get("/"))
        .andReturn();
    String topPageContent = pageResult.getResponse().getContentAsString();
    Document topPageDocument = Jsoup.parse(topPageContent);
    Element detailMenuElement = topPageDocument.selectFirst("a[href='/tweets/" + tweetEntity.getId() + "']");
    assertNotNull(detailMenuElement);
    assertEquals("詳細", detailMenuElement.text());

    // 詳細ページに遷移すると、ツイートの内容が表示されていることを確認する（画像）
    MvcResult detailPageResult = mockMvc.perform(get("/tweets/{tweetId}" ,tweetEntity.getId()))
        .andExpect(status().isOk())
        .andExpect(view().name("tweets/detail"))
        .andReturn();
    String detailPageContent = detailPageResult.getResponse().getContentAsString();
    Document detailPageDocument = Jsoup.parse(detailPageContent);
    Element divElement = detailPageDocument.selectFirst(".content_post[style='background-image: url(" + tweetEntity.getImage() + ");']");
    assertNotNull(divElement);

    // 詳細ページに遷移すると、ツイートの内容が表示されていることを確認する（テキスト）
    mockMvc.perform(get("/tweets/{tweetId}", tweetEntity.getId()))
        .andExpect(content().string(containsString(tweetEntity.getText())));

   // コメント用のフォームが存在しないことを確認する
   Element commentFormElement = detailPageDocument.selectFirst("form");
   assertNull(commentFormElement);

    //「コメントの投稿には新規登録/ログインが必要です」が表示されていることを確認する
    mockMvc.perform(get("/tweets/{tweetId}", tweetEntity.getId()))
        .andExpect(content().string(containsString("コメントの投稿には新規登録/ログインが必要です")));  }
}



//※循環参照に注意！
//循環参照：　２つ以上のオブジェクトがお互いを参照しあっていることにより、参照先を辿りあってしまい無限ループが発生してしまうという問題

//@ToString.Exclude：　toString() メソッドの出力から特定のフィールドを除外するために使用