package in.tech_camp.pictweet.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import in.tech_camp.pictweet.custom_user.CustomUserDetail;
import in.tech_camp.pictweet.entity.TweetEntity;
import in.tech_camp.pictweet.form.CommentForm;
import in.tech_camp.pictweet.form.SearchForm;
import in.tech_camp.pictweet.form.TweetForm;
import in.tech_camp.pictweet.repository.TweetRepository;
import in.tech_camp.pictweet.validation.ValidationOrder;
import lombok.AllArgsConstructor;


@Controller
@AllArgsConstructor
public class TweetController {
  private final TweetRepository TweetRepository;

  @GetMapping("/") //@GetMapping 特定のURLにアクセスしたときに、そのメソッドが実行されるようにな
  public String showTweets(Model model) {
    List<TweetEntity> tweets = TweetRepository.findAll();
    SearchForm searchForm = new SearchForm();
    model.addAttribute("tweets", tweets);
    model.addAttribute("searchForm", searchForm);
    return "tweets/index";  }
    
  @GetMapping("/tweets/new")
  public String showTweetNew(Model model){
    model.addAttribute("tweetForm", new TweetForm());    
    return "tweets/new";
  }
    //@AllArgsConstructorを定義していても、引数なしのコンストラクタが必要な場合は、newを使用してobjectを生成する必要がある
    //※@NoArgsConstructorを併用していない限り、引数なしのコンストラクタは生成されないため

  @PostMapping("/tweets")
  public String createTweet(@ModelAttribute("tweetForm") @Validated TweetForm tweetForm,
                            BindingResult result, 
                            @AuthenticationPrincipal CustomUserDetail currentUser,
                            Model model) {

    if (result.hasErrors()) {
// (result.hasErrors())で、チェック結果にエラーがあるかを確認
      List<String> errorMessages = result.getAllErrors().stream()
              .map(DefaultMessageSourceResolvable::getDefaultMessage)
              .collect(Collectors.toList());
      model.addAttribute("errorMessages", errorMessages);
      model.addAttribute("tweetForm", tweetForm);
//retuenする前に、上記List～で、resultからエラー文を取得し、ListにしてModelに格納している

      return "tweets/new";//エラーが出た時に再度投稿画面を表示さる記述を入れておく
    }

    TweetEntity tweet = new TweetEntity();
    tweet.setUserId(currentUser.getId());
    // 削除　tweet.setName(tweetForm.getName());
    tweet.setText(tweetForm.getText());
    tweet.setImage(tweetForm.getImage());

    try {
      TweetRepository.insert(tweet);
    } catch (Exception e) {
      System.out.println("エラー：" + e);
      return "redirect:/";
    }

    return "redirect:/";
  }

  @PostMapping("/tweets/{tweetId}/delete")
  public String deleteTweet(@PathVariable("tweetId") Integer tweetId) {
    // @PathVariable：パス内に埋め込まれた値を取得するために使用するアノテーション
    try {
      TweetRepository.deleteById(tweetId);
    } catch (Exception e) {
      System.out.println("エラー：" + e);
      return "redirect:/";
    }
    return "redirect:/";
  }

  @GetMapping("/tweets/{tweetId}/edit")
  public String editTweet(@PathVariable("tweetId") Integer tweetId, Model model) {
    TweetEntity tweet = TweetRepository.findById(tweetId);

    TweetForm tweetForm = new TweetForm();
    // 削除　tweetForm.setName(tweet.getName());
    tweetForm.setText(tweet.getText());
    tweetForm.setImage(tweet.getImage());

    model.addAttribute("tweetForm", tweetForm);
    model.addAttribute("tweetId", tweetId);
    return "tweets/edit";
  }

  @PostMapping("/tweets/{tweetId}/update")
  public String createTweet(@ModelAttribute("tweetForm") @Validated(ValidationOrder.class) TweetForm tweetForm,
  // public String updateTweet(@ModelAttribute("tweetForm") @Validated TweetForm tweetForm,
                            BindingResult result,
                            @PathVariable("tweetId") Integer tweetId,
                            Model model) {

    if (result.hasErrors()) {
      List<String> errorMessages = result.getAllErrors().stream()
              .map(DefaultMessageSourceResolvable::getDefaultMessage)
              .collect(Collectors.toList());
      model.addAttribute("errorMessages", errorMessages);

      model.addAttribute("tweetForm", tweetForm);
      model.addAttribute("tweetId", tweetId);
      return "tweets/edit";
    }

    TweetEntity tweet = TweetRepository.findById(tweetId);
    // 削除　tweet.setName(tweetForm.getName());
    tweet.setText(tweetForm.getText());
    tweet.setImage(tweetForm.getImage());

    try {
      TweetRepository.update(tweet);
    } catch (Exception e) {
      System.out.println("エラー：" + e);
      return "redirect:/";
    }

    return "redirect:/";
  }

  @GetMapping("/tweets/{tweetId}")
  public String showTweetDetail(@PathVariable("tweetId") Integer tweetId, Model model) {
      TweetEntity tweet = TweetRepository.findById(tweetId);
      CommentForm commentForm = new CommentForm();
      model.addAttribute("tweet", tweet);
      model.addAttribute("commentForm", commentForm);
      model.addAttribute("comments", tweet.getComments());
      return "tweets/detail";
  }

  @GetMapping("/tweets/search")
  public String searchTweets(@ModelAttribute("searchForm") SearchForm searchForm, Model model) {
    List<TweetEntity> tweets = TweetRepository.findByTextContaining(searchForm.getText());
    model.addAttribute("tweets", tweets);
    model.addAttribute("searchForm", searchForm);
    return "tweets/search";
  }
}