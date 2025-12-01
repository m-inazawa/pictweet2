package in.tech_camp.pictweet.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import in.tech_camp.pictweet.entity.UserEntity;
import in.tech_camp.pictweet.repository.UserRepository;
import lombok.AllArgsConstructor;

@Service //これにより、UserServiceのクラスは、serviceクラスとして動作するようになる
@AllArgsConstructor
public class UserService {
  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  public void createUserWithEncryptedPassword(UserEntity userEntity) {
    // createUserWithEncryptedPasswordは、引数で保存したいUserEntityを受け取り、リポジトリ経由で保存するメソッド
    String encodedPassword = encodePassword(userEntity.getPassword());
    userEntity.setPassword(encodedPassword);
    // encodePassword：PasswordEncoderという暗号化をおこなうクラスを使用し、引数で渡された文字列を暗号化している
    userRepository.insert(userEntity);
  }

  private String encodePassword(String password) {
    return passwordEncoder.encode(password);
  }
}