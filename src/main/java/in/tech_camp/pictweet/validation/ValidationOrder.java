package in.tech_camp.pictweet.validation;

import jakarta.validation.GroupSequence;

@GroupSequence({ ValidationPriority1.class, ValidationPriority2.class})
public interface ValidationOrder {

}

// ①グループの優先度を指定するインターフェース（ValidationPriority1,ValidationPriority2）を用意
// ②@GroupSequenceを付与することで、実行順を指定する。
// 実際に処理を行うものではないため、中身は空で問題ない。