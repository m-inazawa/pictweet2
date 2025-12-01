// ファイル名は任意の名前でOK
// 一般的には、テスト対象クラス名の末尾にTestを追加したもの、
//            テストの種類に合わせて単体テストの場合はUnitTest、結合テストの場合はIntegrationTestを付ける

package in.tech_camp.pictweet.form;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.BindingResult;

import in.tech_camp.pictweet.factory.UserFormFactory;
import in.tech_camp.pictweet.validation.ValidationPriority1;
import in.tech_camp.pictweet.validation.ValidationPriority2;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@ActiveProfiles("test")
// @ActiveProfilesは、Spring Frameworkにおいて、特定のプロファイルをアクティブにするためのアノテーション
public class UserFormUnitTest {
    private UserForm userForm;
    private Validator validator;
    private BindingResult bindingResult;

    @BeforeEach //各テストメソッドの実行前に呼び出されるメソッド
    public void setUp() {//SetUpメソッド内にインスタンス生成処理を記述
        ValidatorFactory factory  = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        userForm = UserFormFactory.createUser();
        bindingResult = Mockito.mock(BindingResult.class); 
//Mock:検査したいメソッドで使用するオブジェクトの代わりとなり、同様の振る舞いをするダミーのオブジェクト
//Mockito(モッキート):Java向けのモックオブジェクト生成ライブラリ 
    }

    @Nested//テストメソッドをグループ化する
    class UserCanCreate {
        @Test
        public void nicknameとemailとpasswordとpasswordConfirmationが存在すれば登録できる() {
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority1.class);
            assertEquals(0, violations.size());
        }
    }

    @Nested//テストメソッドをグループ化する
    class UserCannotCreate {
        @Test
        public void nicknameが空の場合バリデーションエラーが発生する() {
            userForm.setNickname("");
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority1.class);
            assertEquals(1, violations.size());
            assertEquals("Nickname can't be blank", violations.iterator().next().getMessage());        
        }
        
        @Test
        public void emailが空の場合バリデーションエラーが発生する() {
            userForm.setEmail(""); // 空のメール
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority1.class);
            assertEquals(1, violations.size());
            assertEquals("Email can't be blank", violations.iterator().next().getMessage());
        }

        @Test
        public void passwordが空の場合バリデーションエラーが発生する() {
            userForm.setPassword("");
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority1.class);
            assertEquals(1, violations.size());
            assertEquals("Password can't be blank", violations.iterator().next().getMessage());
        }
    
        @Test
        public void passwordとpasswordConfirmationが不一致ではバリデーションエラーが発生する() {
            userForm.setPasswordConfirmation("differentPassword");//  異なるパスワードを設定
            userForm.validatePasswordConfirmation(bindingResult);//  バリデーションメソッドを呼び出す
            verify(bindingResult).rejectValue("passwordConfirmation", null, "Password confirmation doesn't match Password");//  結果とエラーメッセージの一致を確認
        }
    
        @Test
        public void nicknameが7文字以上ではバリデーションエラーが発生する() {
            userForm.setNickname("TooLong");
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority2.class);
            assertEquals(1, violations.size());
            assertEquals("Nickname is too long (maximum is 6 characters)", violations.iterator().next().getMessage());
        }
    
        @Test
        public void emailはアットマークを含まないとバリデーションエラーが発生する() {
            userForm.setEmail("invalidEmail");
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority2.class);
            assertEquals(1, violations.size());
            assertEquals("Email should be valid", violations.iterator().next().getMessage());
        }
    
        @Test
        public void passwordが5文字以下ではバリデーションエラーが発生する() {
            String password = "a".repeat(5);
            userForm.setPassword(password);
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority2.class);
            assertEquals(1, violations.size());
            assertEquals("Password should be between 6 and 128 characters", violations.iterator().next().getMessage());
        }
    
        @Test
        public void passwordが129文字以上ではバリデーションエラーが発生する() {
            String password = "a".repeat(129);
            userForm.setPassword(password);
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority2.class);
            violations.forEach(violation -> System.out.println(violation.getMessage()));
            assertEquals(1, violations.size());
            assertEquals("Password should be between 6 and 128 characters", violations.iterator().next().getMessage());
        }
    }
}




// 作成例　@Test　userForm.setNickname
        // UserFormのインスタンスを初期化
// Factoryクラスを利用する場合　↓
        // UserForm userForm = UserFormFactory.createUser();
        // userForm.setNickname("");
// Factoryクラスを利用しない場合　↓
        // UserForm userForm = new UserForm();
        // userForm.setNickname(""); // 空のニックネーム
        // userForm.setEmail("test@test.com"); // メールアドレス
        // userForm.setPassword("techcamp123"); // パスワード
        // userForm.setPasswordConfirmation("techcamp123");// 確認用パスワード


        // バリデーションの実行
        // ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        // Validator validator = factory.getValidator();
        // Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority1.class);
            // ConstraintViolation：バリデーションチェックの結果として生成されるオブジェクト。
            // userFormの内容に対するバリデーションを実行
            // バリデーションチェックの結果、エラーが発生した場合、エラー情報を含むConstraintViolationオブジェクトが作成される。
            //　　（バリデーションエラーに関する情報がviolationsに格納される）

        //アサーション
        // assertEquals(1, violations.size());
        // assertEquals("Nickname can't be blank", violations.iterator().next().getMessage());        
        // 【staticインポート】staticインポートを使用すると、インスタンスを作成せずに呼び出すことができる
        // 通常：Assertions.assertThat(実測値, 期待値);と記載
        // staticインポートを利用時：assertThat(実測値, 期待値);
        // メリット：自然な英語に近い表現でテストを記述することができる
// violationsは一つの集合であり、要素の順番は決まっていない
// そのため、iterator()メソッドを使って、集合の中のデータを順番に確認できるようにする 
// next()メソッドを使うことで最初のバリデーションエラーの情報を取り出す
// エラーが発生した場合に、.getMessage()でエラーメッセージを取得する

