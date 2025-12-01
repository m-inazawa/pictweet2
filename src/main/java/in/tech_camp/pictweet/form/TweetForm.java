package in.tech_camp.pictweet.form;

import in.tech_camp.pictweet.validation.ValidationPriority1;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TweetForm {
  // 削除　private String name;

  @NotBlank(message = "Text can't be blank", groups = ValidationPriority1.class)
  //NotBlank　バリデーションのためのアノテーション
      //空では保存できないようにする役割
  private String text;

  private String image;
}
