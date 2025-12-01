package in.tech_camp.pictweet.controller;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import in.tech_camp.pictweet.entity.TweetEntity;
import in.tech_camp.pictweet.form.SearchForm;
import in.tech_camp.pictweet.repository.TweetRepository;



@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class TweetControllerUnitTest {
  @Mock//モックオブジェクトを作成
  private TweetRepository tweetRepository;

  @InjectMocks//モックの注入先を指定 
  private TweetController tweetController;

  private Model model;

  @BeforeEach
  public void setUp() {
    model = new ExtendedModelMap();
    // ExtendedModelMapは、SpringフレームワークにおけるModelインターフェースの実装の一つ
  }

  @Test
  public void 投稿機能にリクエストするとツイート一覧表示のビューファイルがレスポンスで返ってくる() {
 
    String result = tweetController.showTweets(model);//test対象のメソッドを呼び出す
 
    assertThat(result, is("tweets/index"));//結果が期待通りであるかassertionを用いて確認
//MatcherAssert.assertThat(実測値, マッチャオブジェクト); 
  }

  @Test
  public void 投稿機能にリクエストするとレスポンスに投稿済みのツイートがすべて含まれること() {
    TweetEntity tweet1 = new TweetEntity();
    tweet1.setId(1);
    tweet1.setText("ツイート1");
    tweet1.setImage("image1.jpg");

    TweetEntity tweet2 = new TweetEntity();
    tweet1.setId(2);
    tweet1.setText("ツイート2");
    tweet1.setImage("image2.jpg");
    
    List<TweetEntity> expectedTweetList = Arrays.asList(tweet1, tweet2);

    when(tweetRepository.findAll()).thenReturn(expectedTweetList);
    //when：when()に続けて指定したメソッドが呼ばれた際、どのようにふるまうかを指定する
    //thenReturn：when()で指定したメソッドの戻り値を指定する

    tweetController.showTweets(model);

    assertThat(model.getAttribute("tweets"), is(expectedTweetList));
  }

  @Test
  public void 投稿一覧機能にリクエストするとレスポンスに投稿検索フォームが存在する() {
    SearchForm searchForm = new SearchForm(); // 新しいSearchFormオブジェクトを作成
    tweetController.showTweets(model); // コントローラーのshowTweetsメソッドを呼び出す
    // モデルオブジェクトから検索フォームを取得し、期待されるformと比較
    assertThat(model.getAttribute("searchForm"), is(searchForm));
    // model.getAttribute("searchForm")でモデルオブジェクトから"SearchForm"属性を取得
    // is(searchForm)で期待するSearchFormオブジェクトと比較している
  }
}
