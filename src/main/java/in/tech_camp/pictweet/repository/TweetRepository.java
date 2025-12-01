package in.tech_camp.pictweet.repository;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import in.tech_camp.pictweet.entity.TweetEntity;

@Mapper
public interface TweetRepository {

  // @Select("SELECT * FROM tweets")
  // @Results(value = {
  //   @Result(property = "user", column = "user_id",
  //           one = @One(select = "in.tech_camp.pictweet.repository.UserRepository.findById"))
  // })
  // List<TweetEntity> findAll();
// ↑の処理を、↓のようにSQL JOIN句を使用してテーブル結合を行い、N+1問題が起こり得るfindAll()の対策を行う
@Select("SELECT t.*, u.id AS user_id, u.nickname AS user_nickname FROM tweets t JOIN users u ON t.user_id = u.id ORDER BY t.created_at DESC")
  @Results(value = {
      @Result(property = "user.id", column = "user_id"),
      @Result(property = "user.nickname", column = "user_nickname")
  })
  List<TweetEntity> findAll();


  @Insert("INSERT INTO tweets (text, image, user_id) VALUES (#{text}, #{image}, #{userId})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  // useGeneratedKeys = true　は、保存時にデータベースによって生成されたキーをアプリケーション側で取得することができる
  // keyProperty = "id"　は、生成されたキーをどのプロパティに設定するかを指定する
  void insert(TweetEntity tweet);

  @Delete("DELETE FROM tweets WHERE id = #{id}")
  void deleteById(Integer id);

  @Select("SELECT * FROM tweets WHERE id = #{id}")
  @Results(value = {
    @Result(property = "id", column = "id"),
    @Result(property = "user", column = "user_id",
            one = @One(select = "in.tech_camp.pictweet.repository.UserRepository.findById")),
    @Result(property = "comments", column = "id", 
            many = @Many(select = "in.tech_camp.pictweet.repository.CommentRepository.findByTweetId"))
  })
  TweetEntity findById(Integer id);

  @Update("UPDATE tweets SET text = #{text}, image = #{image} WHERE id = #{id}")
  void update(TweetEntity tweet);

// 検索用
  @Select("SELECT * FROM tweets WHERE text LIKE CONCAT('%', #{text}, '%')")
  @Results(value = {
    @Result(property = "user", column = "user_id",
            one = @One(select = "in.tech_camp.pictweet.repository.UserRepository.findById"))
  })
  List<TweetEntity> findByTextContaining(String text);


}