package in.tech_camp.pictweet.system;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import in.tech_camp.pictweet.PictweetApplication;
import in.tech_camp.pictweet.entity.CommentEntity;
import in.tech_camp.pictweet.entity.TweetEntity;
import in.tech_camp.pictweet.entity.UserEntity;
import in.tech_camp.pictweet.factory.CommentFormFactory;
import in.tech_camp.pictweet.factory.TweetFormFactory;
import in.tech_camp.pictweet.factory.UserFormFactory;
import in.tech_camp.pictweet.form.CommentForm;
import in.tech_camp.pictweet.form.TweetForm;
import in.tech_camp.pictweet.form.UserForm;
import in.tech_camp.pictweet.repository.CommentRepository;
import in.tech_camp.pictweet.repository.TweetRepository;
import in.tech_camp.pictweet.service.UserService;
import static in.tech_camp.pictweet.support.LoginSupport.login;

@ActiveProfiles("test")
@SpringBootTest(classes = PictweetApplication.class)
@AutoConfigureMockMvc
public class CommentIntegrationTest {
  private UserForm userForm;
  private UserEntity userEntity;

  private TweetForm tweetForm;
  private TweetEntity tweetEntity;

  private CommentForm commentForm;

  private int initialCount;
  private int afterCount;

  @Autowired
  private UserService userService;

  @Autowired
  private TweetRepository tweetRepository;

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private MockMvc mockMvc;

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

    commentForm = CommentFormFactory.createComment();
  }

  @Test
  public void ログインしたユーザーはツイート詳細ページでコメント投稿できる() throws Exception {
    // ログインする
    MvcResult loginResult = mockMvc.perform(post("/login")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("email", userForm.getEmail())
        .param("password", userForm.getPassword())
        .with(csrf()))
        .andReturn();

    MockHttpSession session = login(mockMvc, userForm);
    assertNotNull(session);

    // ツイート詳細ページに遷移する
    mockMvc.perform(get("/tweets/{tweetId}", tweetEntity.getId()).session(session))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(tweetEntity.getText())));

    List<CommentEntity> commentsListBeforePost = commentRepository.findByTweetId(tweetEntity.getId());
    initialCount = commentsListBeforePost.size();

    // フォームに情報を入力し投稿する
    mockMvc.perform(post("/tweets/{tweetId}/comment", tweetEntity.getId()).session(session)
            .param("text", commentForm.getText())
            .with(csrf()))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/tweets/" + tweetEntity.getId()));

    // コメントを送信すると、Commentモデルのカウントが1上がる
    List<CommentEntity> commentsListAfterPost = commentRepository.findByTweetId(tweetEntity.getId());
    afterCount = commentsListAfterPost.size();
    assertEquals(initialCount + 1, afterCount);

    // 詳細ページに再度アクセスして、コメント内容を確認
    mockMvc.perform(get("/tweets/{tweetId}", tweetEntity.getId()).session(session))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(commentForm.getText())));
  }
}