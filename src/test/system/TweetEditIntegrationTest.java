package in.tech_camp.pictweet.system;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import in.tech_camp.pictweet.entity.TweetEntity;
import in.tech_camp.pictweet.entity.UserEntity;
import in.tech_camp.pictweet.factory.TweetFormFactory;
import in.tech_camp.pictweet.factory.UserFormFactory;
import in.tech_camp.pictweet.form.TweetForm;
import in.tech_camp.pictweet.form.UserForm;
import in.tech_camp.pictweet.repository.TweetRepository;
import in.tech_camp.pictweet.service.UserService;
import static in.tech_camp.pictweet.support.LoginSupport.login;

@ActiveProfiles("test")
@SpringBootTest(classes = PictweetApplication.class)
@AutoConfigureMockMvc
public class TweetEditIntegrationTest {
  private UserForm userForm1;
  private UserEntity userEntity1;

  private UserForm userForm2;
  private UserEntity userEntity2;

  private TweetForm tweetForm1;
  private TweetEntity tweetEntity1;

  private TweetForm tweetForm2;
  private TweetEntity tweetEntity2;

  private int initialCount;
  private int afterCount;

  @Autowired
  private UserService userService;

  @Autowired
  private TweetRepository tweetRepository;

  @Autowired
  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    userForm1 = UserFormFactory.createUser();
    userEntity1 = new UserEntity();
    userEntity1.setEmail(userForm1.getEmail());
    userEntity1.setNickname(userForm1.getNickname());
    userEntity1.setPassword(userForm1.getPassword());
    userService.createUserWithEncryptedPassword(userEntity1);

    userForm2 = UserFormFactory.createUser();
    userEntity2 = new UserEntity();
    userEntity2.setEmail(userForm2.getEmail());
    userEntity2.setNickname(userForm2.getNickname());
    userEntity2.setPassword(userForm2.getPassword());
    userService.createUserWithEncryptedPassword(userEntity2);

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
  class ツイート編集できる時 {
    @Test
    public void ログインしたユーザーは自分が投稿したツイートの編集ができる() throws Exception {
      // ツイート1を投稿したユーザーでログインする
      MvcResult loginResult = mockMvc.perform(post("/login")
      //MvcResult loginResult =：シミュレートされたリクエストの実行結果（レスポンス、ステータス、セッションなど）を MvcResult オブジェクトに保存
      //mockMvc.perform(post("/login")：仮想のHTTP環境を提供する MockMvc を使用して、定義したリクエストを実行
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)//送信するデータの形式が、標準的なHTMLフォームの形式（application/x-www-form-urlencoded）であることを指定
        .param("email", userForm1.getEmail())//(フィールド名, setする値)
        .param("password", userForm1.getPassword())
        .with(csrf()))
        .andReturn();

      MockHttpSession session = login(mockMvc, userForm1);
      assertNotNull(session);

      // ツイート1に「編集」へのリンクがあることを確認する
      MvcResult pageResult = mockMvc.perform(get("/").session(session))
        .andReturn();
        String topPageContent = pageResult.getResponse().getContentAsString();
        Document topPageDocument = Jsoup.parse(topPageContent);
        Element editMenuElement = topPageDocument.selectFirst("a[href='/tweets" + tweetEntity1.getId() + "/edit']");
        assertNotNull(editMenuElement);
        assertEquals("編集", editMenuElement.text());


      // 編集ページへ遷移し、すでに投稿済みの内容がフォームに入っていることを確認する
      mockMvc.perform(get("/tweets/{tweetId}/edit", tweetEntity1.getId()).session(session))
        .andExpect(status().isOk())
        .andExpect(view().name("tweets/edit"))
        .andExpect(content().string(containsString(tweetEntity1.getText())))
        .andExpect(content().string(containsString(tweetEntity1.getImage())));

      List<TweetEntity> tweetsListBeforeEdit = tweetRepository.findAll();
      initialCount = tweetsListBeforeEdit.size();

      // 投稿内容を編集する
      mockMvc.perform(post("/tweets/tweetId}/update", tweetEntity1.getId()).session(session)
        .param("text", tweetEntity1.getId() + "編集したテキスト")
        .param("image", tweetEntity1.getImage() + "編集した画像URL")
        .with(csrf()))
        .andExpect(status().isFound())//applicationが、redirect先に飛びなさいという指示を出したかを確認している
          //.isFound()：HTTPステータスコードが「302 Found」 であることを検証（アサート）している
          // クライアント（ブラウザ）に対して「一時的に別のURLに移動（リダイレクト）してください」という指示を伝えるために使用される
        .andExpect(redirectedUrl("/"));//リクエストから、アプリケーションが指定しているredirect先が/であったかを確認している

        // 編集してもtweetsテーブルのレコードの数が変わらないことを確認する
      List<TweetEntity> tweetsListAfterEdit = tweetRepository.findAll();
      afterCount = tweetsListAfterEdit.size();
      assertEquals(initialCount, afterCount);

      // トップページには先ほど変更した内容のツイートが存在することを確認する（画像）
      MvcResult pageResultAfterEdit = mockMvc.perform(get("/"))
        .andReturn();
      String pageContentAfterEdit = pageResultAfterEdit.getResponse().getContentAsString();
      Document documentAfterEdit = Jsoup.parse(pageContentAfterEdit);
      Element divElement = documentAfterEdit.selectFirst(".content_post[style='background-image: url(" + tweetForm1.getImage() + "編集した画像URL" + ");']");
      assertNotNull(divElement);

      // トップページには先ほど変更した内容のツイートが存在することを確認する（テキスト）
      mockMvc.perform(get("/"))
        .andExpect(content().string(containsString(tweetForm1.getText() + "編集したテキスト")));
    }
  }

  @Nested
  class ツイート編集できないとき {
    @Test
    public void ログインしたユーザーは自分以外が投稿したツイートの編集画面には遷移できない() throws Exception {
      // ツイート1を投稿したユーザーでログインする
      MvcResult loginResult = mockMvc.perform(post("/login")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", userForm1.getEmail())
        .param("password", userForm1.getPassword())
        .with(csrf()))
        .andReturn();

      MockHttpSession session = (MockHttpSession)loginResult.getRequest().getSession();
      assertNotNull(session);

      // ツイート2に「編集」へのリンクがないことを確認する
      MvcResult PageResult = mockMvc.perform(get("/").session(session))
        .andReturn();
      String pageContent = PageResult.getResponse().getContentAsString();
      Document document = Jsoup.parse(pageContent);
      Element editMenuElement = document.selectFirst("a[href='/tweets/" + tweetEntity2.getId() + "/edit']");
      assertNull(editMenuElement);
    }

    @Test
    public void ログインしていないとツイートの編集画面には遷移できない() throws Exception {
      // ログインせずにトップページにアクセス
      MvcResult pageResult = mockMvc.perform(get("/"))
        .andReturn();
      String pageContent = pageResult.getResponse().getContentAsString();
      Document document = Jsoup.parse(pageContent);

      // ツイート1に「編集」へのリンクがないことを確認する
      Element tweet1editMenuElement = document.selectFirst("a[href='/tweets/" + tweetEntity1.getId() + "/edit']");// 編集リンクをCSSセレクタで取得
      assertNull(tweet1editMenuElement);
      // ツイート2に「編集」へのリンクがないことを確認する
      Element tweet2editMenuElement = document.selectFirst("a[href='/tweets/" + tweetEntity2.getId() + "/edit']");// 編集リンクをCSSセレクタで取得
      assertNull(tweet2editMenuElement);
    }
  }
}
