package in.tech_camp.pictweet.entity;

import java.sql.Timestamp;
import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
public class TweetEntity {
  private Integer id;
  // 削除　private String name;
  private String text;
  private String image;
  private Timestamp createdAt;
  @ToString.Exclude
  private Integer userId;
  private UserEntity user;
  @ToString.Exclude
  //@ToString.Exclude：　toString() メソッドの出力から特定のフィールドを除外するために使用
  private List<CommentEntity> comments;
}

//privateなフィールドは外部から値を入出力するためのメソッドを用意する必要がある
//※クラスに@Dataを付与することで、Lombokの機能で専用のメソッドが自動生成されるため、記述する必要がなくなる