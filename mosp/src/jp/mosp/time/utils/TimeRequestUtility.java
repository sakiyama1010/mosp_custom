/*
 * MosP - Mind Open Source Project    http://www.mosp.jp/
 * Copyright (C) MIND Co., Ltd.       http://www.e-mind.co.jp/
 * 
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jp.mosp.time.utils;

import java.util.Collection;

import jp.mosp.framework.utils.MospUtility;
import jp.mosp.time.constant.TimeConst;
import jp.mosp.time.dto.base.HolidayRangeDtoInterface;
import jp.mosp.time.dto.settings.HolidayRequestDtoInterface;
import jp.mosp.time.dto.settings.SubHolidayRequestDtoInterface;

/**
 * 申請における有用なメソッドを提供する。<br>
 */
public class TimeRequestUtility {
	
	/**
	 * 他クラスからのインスタンス化を防止する。<br>
	 */
	private TimeRequestUtility() {
		// 処理無し
	}
	
	/**
	 * 休暇申請情報が有給休暇であるかを確認する。<br>
	 * @param dto 休暇申請情報
	 * @return 確認結果(true：有給休暇である、false：有給休暇でない)
	 */
	public static final boolean isPaidHoliday(HolidayRequestDtoInterface dto) {
		// 休暇申請情報が有給休暇であるかを確認
		return isTheHolidayType(dto, TimeConst.CODE_HOLIDAYTYPE_HOLIDAY, TimeConst.CODE_HOLIDAYTYPE2_PAID);
	}
	
	/**
	 * 休暇申請情報がストック休暇であるかを確認する。<br>
	 * @param dto 休暇申請情報
	 * @return 確認結果(true：ストック休暇である、false：ストック休暇でない)
	 */
	public static final boolean isStockHoliday(HolidayRequestDtoInterface dto) {
		// 休暇申請情報がストック休暇であるかを確認
		return isTheHolidayType(dto, TimeConst.CODE_HOLIDAYTYPE_HOLIDAY, TimeConst.CODE_HOLIDAYTYPE2_STOCK);
	}
	
	/**
	 * 休暇申請情報が特別休暇であるかを確認する。<br>
	 * @param dto 休暇申請情報
	 * @return 確認結果(true：特別休暇である、false：特別休暇でない)
	 */
	public static final boolean isSpecialHoliday(HolidayRequestDtoInterface dto) {
		// 休暇申請情報が有給休暇であるかを確認
		return isTheHolidayType1(dto, TimeConst.CODE_HOLIDAYTYPE_SPECIAL);
	}
	
	/**
	 * 休暇申請情報がその他休暇であるかを確認する。<br>
	 * @param dto 休暇申請情報
	 * @return 確認結果(true：その他休暇である、false：その他休暇でない)
	 */
	public static final boolean isOtherHoliday(HolidayRequestDtoInterface dto) {
		// 休暇申請情報が有給休暇であるかを確認
		return isTheHolidayType1(dto, TimeConst.CODE_HOLIDAYTYPE_OTHER);
	}
	
	/**
	 * 休暇申請情報が欠勤であるかを確認する。<br>
	 * @param dto 休暇申請情報
	 * @return 確認結果(true：欠勤である、false：欠勤でない)
	 */
	public static final boolean isAbsenece(HolidayRequestDtoInterface dto) {
		// 休暇申請情報が有給休暇であるかを確認
		return isTheHolidayType1(dto, TimeConst.CODE_HOLIDAYTYPE_ABSENCE);
	}
	
	/**
	 * 休暇申請情報がその休暇種別1であるかを確認する。<br>
	 * @param dto          休暇申請情報
	 * @param holidayType1 休暇種別1
	 * @return 確認結果(true：その休暇種別1である、false：その休暇種別1でない)
	 */
	protected static final boolean isTheHolidayType1(HolidayRequestDtoInterface dto, int holidayType1) {
		// 休暇申請情報が存在しない場合
		if (dto == null) {
			// その休暇種別1でないと判断
			return false;
		}
		// 休暇申請情報がその休暇種別1であるかを確認
		return dto.getHolidayType1() == holidayType1;
	}
	
	/**
	 * 休暇申請情報がその休暇種別2であるかを確認する。<br>
	 * @param dto          休暇申請情報
	 * @param holidayType2 休暇種別2
	 * @return 確認結果(true：その休暇種別2である、false：その休暇種別2でない)
	 */
	protected static final boolean isTheHolidayType2(HolidayRequestDtoInterface dto, String holidayType2) {
		// 休暇申請情報が存在しない場合
		if (dto == null || MospUtility.isEmpty(dto.getHolidayType2())) {
			// その休暇種別2でないと判断
			return false;
		}
		// 休暇申請情報がその休暇種別2であるかを確認
		return dto.getHolidayType2().equals(holidayType2);
	}
	
	/**
	 * 休暇申請情報がその休暇種別であるかを確認する。<br>
	 * @param dto          休暇申請情報
	 * @param holidayType1 休暇種別1
	 * @param holidayType2 休暇種別2
	 * @return 確認結果(true：その休暇種別1である、false：その休暇種別1でない)
	 */
	protected static final boolean isTheHolidayType(HolidayRequestDtoInterface dto, int holidayType1,
			String holidayType2) {
		// 休暇種別2が空白である場合
		if (MospUtility.isEmpty(holidayType2)) {
			// 休暇申請情報がその休暇種別1であるかを確認
			return isTheHolidayType1(dto, holidayType1);
		}
		// 休暇申請情報がその休暇種別であるかを確認
		return isTheHolidayType1(dto, holidayType1) && isTheHolidayType2(dto, holidayType2);
	}
	
	/**
	 * 休暇(範囲)情報が全休であるかを確認する。<br>
	 * @param dto 休暇(範囲)情報
	 * @return 確認結果(true：全休である、false：全休でない)
	 */
	public static boolean isHolidayRangeAll(HolidayRangeDtoInterface dto) {
		// 休暇(範囲)情報が全休であるかを確認
		return isTheHolidayRange(dto, TimeConst.CODE_HOLIDAY_RANGE_ALL);
	}
	
	/**
	 * 休暇(範囲)情報が前半休であるかを確認する。<br>
	 * @param dto 休暇(範囲)情報
	 * @return 確認結果(true：前半休である、false：前半休でない)
	 */
	public static boolean isHolidayRangeAm(HolidayRangeDtoInterface dto) {
		// 休暇(範囲)情報が前半休であるかを確認
		return isTheHolidayRange(dto, TimeConst.CODE_HOLIDAY_RANGE_AM);
	}
	
	/**
	 * 休暇(範囲)情報が後半休であるかを確認する。<br>
	 * @param dto 休暇(範囲)情報
	 * @return 確認結果(true：後半休である、false：後半休でない)
	 */
	public static boolean isHolidayRangePm(HolidayRangeDtoInterface dto) {
		// 休暇(範囲)情報が後半休であるかを確認
		return isTheHolidayRange(dto, TimeConst.CODE_HOLIDAY_RANGE_PM);
	}
	
	/**
	 * 休暇(範囲)情報が半休であるかを確認する。<br>
	 * @param dto 休暇(範囲)情報
	 * @return 確認結果(true：半休である、false：半休でない)
	 */
	public static boolean isHolidayRangeHalf(HolidayRangeDtoInterface dto) {
		// 休暇(範囲)情報が半休であるかを確認
		return isHolidayRangeAm(dto) || isHolidayRangePm(dto);
	}
	
	/**
	 * 休暇(範囲)情報が時間休であるかを確認する。<br>
	 * @param dto 休暇(範囲)情報
	 * @return 確認結果(true：時間休である、false：時間休でない)
	 */
	public static boolean isHolidayRangeHour(HolidayRangeDtoInterface dto) {
		// 休暇(範囲)情報が時間休であるかを確認
		return isTheHolidayRange(dto, TimeConst.CODE_HOLIDAY_RANGE_TIME);
	}
	
	/**
	 * 休暇(範囲)情報群に全休があるかを確認する。<br>
	 * @param dtos 休暇(範囲)情報群
	 * @return 確認結果(true：全休がある、false：全休がない)
	 */
	public static boolean hasHolidayRangeAll(Collection<? extends HolidayRangeDtoInterface> dtos) {
		// 休暇(範囲)情報が全休であるかを確認
		return hasTheHolidayRange(dtos, TimeConst.CODE_HOLIDAY_RANGE_ALL);
	}
	
	/**
	 * 休暇(範囲)情報群に前半休があるかを確認する。<br>
	 * @param dtos 休暇(範囲)情報群
	 * @return 確認結果(true：前半休がある、false：前半休がない)
	 */
	public static boolean hasHolidayRangeAm(Collection<? extends HolidayRangeDtoInterface> dtos) {
		// 休暇(範囲)情報が前半休であるかを確認
		return hasTheHolidayRange(dtos, TimeConst.CODE_HOLIDAY_RANGE_AM);
	}
	
	/**
	 * 休暇(範囲)情報群に後半休があるかを確認する。<br>
	 * @param dtos 休暇(範囲)情報群
	 * @return 確認結果(true：後半休がある、false：後半休がない)
	 */
	public static boolean hasHolidayRangePm(Collection<? extends HolidayRangeDtoInterface> dtos) {
		// 休暇(範囲)情報が後半休であるかを確認
		return hasTheHolidayRange(dtos, TimeConst.CODE_HOLIDAY_RANGE_PM);
	}
	
	/**
	 * 休暇(範囲)情報がその休暇範囲であるかを確認する。<br>
	 * @param dto          休暇(範囲)情報
	 * @param holidayRange 休暇範囲
	 * @return 確認結果(true：その休暇範囲である、false：その休暇範囲でない)
	 */
	protected static boolean isTheHolidayRange(HolidayRangeDtoInterface dto, int holidayRange) {
		// 休暇(範囲)情報が存在しない場合
		if (dto == null) {
			// その休暇範囲でないと判断
			return false;
		}
		// 休暇(範囲)情報がその休暇範囲であるかを確認
		return dto.getHolidayRange() == holidayRange;
	}
	
	/**
	 * 休暇(範囲)情報群にその休暇範囲があるかを確認する。<br>
	 * @param dtos         休暇(範囲)情報群
	 * @param holidayRange 休暇範囲
	 * @return 確認結果(true：その休暇範囲がある、false：ない)
	 */
	protected static boolean hasTheHolidayRange(Collection<? extends HolidayRangeDtoInterface> dtos, int holidayRange) {
		// 休暇(範囲)情報毎に処理
		for (HolidayRangeDtoInterface dto : dtos) {
			// 休暇(範囲)情報がその休暇範囲である場合
			if (isTheHolidayRange(dto, holidayRange)) {
				// その休暇範囲があると判断
				return true;
			}
		}
		// その休暇範囲がないと判断
		return false;
	}
	
	/**
	 * 休暇範囲の日数を取得する。<br>
	 * 全休と前半休及び後半休を対象とし、時間休は0とする。<br>
	 * @param dto 休暇(範囲)情報
	 * @return 休暇範囲の日数
	 */
	protected static float getDays(HolidayRangeDtoInterface dto) {
		// 全休である場合
		if (isHolidayRangeAll(dto)) {
			// 休暇日数(1)を取得
			return TimeConst.HOLIDAY_TIMES_ALL;
		}
		// 前半休か後半休である場合
		if (isHolidayRangeAm(dto) || isHolidayRangePm(dto)) {
			// 休暇日数(0.5)を取得
			return TimeConst.HOLIDAY_TIMES_HALF;
		}
		// それ以外の場合(0を取得)
		return 0F;
	}
	
	/**
	 * 有給休暇時間数(時間)を集計する。<br>
	 * @param dtos 休暇申請情報群
	 * @return 有給休暇時間数(時間)
	 */
	public static int totalPaidHolidayHours(Collection<HolidayRequestDtoInterface> dtos) {
		// 有給休暇時間数(時間)を集計
		return totalHolidayHours(dtos, TimeConst.CODE_HOLIDAYTYPE_HOLIDAY, TimeConst.CODE_HOLIDAYTYPE2_PAID);
	}
	
	/**
	 * 特別休暇時間数(時間)を集計する。<br>
	 * @param dtos 休暇申請情報群
	 * @return 特別休暇時間数(時間)
	 */
	public static int totalSpecialHolidayHours(Collection<HolidayRequestDtoInterface> dtos) {
		// 特別休暇時間数(時間)を集計
		return totalHolidayHours(dtos, TimeConst.CODE_HOLIDAYTYPE_SPECIAL);
	}
	
	/**
	 * その他休暇時間数(時間)を集計する。<br>
	 * @param dtos 休暇申請情報群
	 * @return その他休暇時間数(時間)
	 */
	public static int totalOtherHolidayHours(Collection<HolidayRequestDtoInterface> dtos) {
		// その他休暇時間数(時間)を集計
		return totalHolidayHours(dtos, TimeConst.CODE_HOLIDAYTYPE_OTHER);
	}
	
	/**
	 * 欠勤時間数(時間)を集計する。<br>
	 * @param dtos 休暇申請情報群
	 * @return 欠勤時間数(時間)
	 */
	public static int totalAbsenceHolidayHours(Collection<HolidayRequestDtoInterface> dtos) {
		// 欠勤時間数(時間)を集計
		return totalHolidayHours(dtos, TimeConst.CODE_HOLIDAYTYPE_ABSENCE);
	}
	
	/**
	 * 休暇時間数(時間)を集計する。<br>
	 * @param dtos         休暇申請情報群
	 * @param holidayType1 休暇区分1
	 * @param holidayType2 休暇区分2
	 * @return 休暇時間数(時間)
	 */
	protected static int totalHolidayHours(Collection<HolidayRequestDtoInterface> dtos, int holidayType1,
			String holidayType2) {
		// 休暇時間数(時間)を準備
		int holidayHours = 0;
		// 休暇申請情報毎に処理
		for (HolidayRequestDtoInterface dto : dtos) {
			// その休暇区分でない場合
			if (isTheHolidayType(dto, holidayType1, holidayType2) == false) {
				// 次の休暇申請情報へ
				continue;
			}
			// 時間単位でない場合
			if (isHolidayRangeHour(dto) == false) {
				// 次の休暇申請情報へ
				continue;
			}
			// 休暇時間数(時間)を加算
			holidayHours += dto.getUseHour();
		}
		// 休暇時間数(時間)を取得
		return holidayHours;
	}
	
	/**
	 * 休暇時間数(時間)を集計する。<br>
	 * @param dtos         休暇申請情報群
	 * @param holidayType1 休暇区分1
	 * @return 休暇時間数(時間)
	 */
	protected static int totalHolidayHours(Collection<HolidayRequestDtoInterface> dtos, int holidayType1) {
		// 休暇時間数(時間)を取得
		return totalHolidayHours(dtos, holidayType1, "");
	}
	
	/**
	 * 有給休暇日数を集計する。<br>
	 * @param dtos 休暇申請情報群
	 * @return 有給休暇日数
	 */
	public static float totalPaidHolidayDays(Collection<HolidayRequestDtoInterface> dtos) {
		// 有給休暇日数を集計
		return totalHolidayDays(dtos, TimeConst.CODE_HOLIDAYTYPE_HOLIDAY, TimeConst.CODE_HOLIDAYTYPE2_PAID);
	}
	
	/**
	 * ストック休暇日数を集計する。<br>
	 * @param dtos 休暇申請情報群
	 * @return ストック休暇日数
	 */
	public static float totalStockHolidayDays(Collection<HolidayRequestDtoInterface> dtos) {
		// ストック休暇日数を集計
		return totalHolidayDays(dtos, TimeConst.CODE_HOLIDAYTYPE_HOLIDAY, TimeConst.CODE_HOLIDAYTYPE2_STOCK);
	}
	
	/**
	 * 特別休暇日数を集計する。<br>
	 * @param dtos 休暇申請情報群
	 * @return 特別休暇日数
	 */
	public static float totalSpecialHolidayDays(Collection<HolidayRequestDtoInterface> dtos) {
		// 特別休暇日数を集計
		return totalHolidayDays(dtos, TimeConst.CODE_HOLIDAYTYPE_SPECIAL);
	}
	
	/**
	 * その他休暇日数を集計する。<br>
	 * @param dtos 休暇申請情報群
	 * @return その他休暇日数
	 */
	public static float totalOtherHolidayDays(Collection<HolidayRequestDtoInterface> dtos) {
		// その他休暇日数を集計
		return totalHolidayDays(dtos, TimeConst.CODE_HOLIDAYTYPE_OTHER);
	}
	
	/**
	 * 欠勤日数を集計する。<br>
	 * @param dtos 休暇申請情報群
	 * @return 欠勤日数
	 */
	public static float totalAbsenceDays(Collection<HolidayRequestDtoInterface> dtos) {
		// 欠勤日数を集計
		return totalHolidayDays(dtos, TimeConst.CODE_HOLIDAYTYPE_ABSENCE);
	}
	
	/**
	 * 休暇日数を集計する。<br>
	 * @param dtos         休暇申請情報群
	 * @param holidayType1 休暇区分1
	 * @param holidayType2 休暇区分2
	 * @return 休暇日数
	 */
	protected static float totalHolidayDays(Collection<HolidayRequestDtoInterface> dtos, int holidayType1,
			String holidayType2) {
		// 休暇日数を準備
		float holidayDays = 0F;
		// 休暇申請情報毎に処理
		for (HolidayRequestDtoInterface dto : dtos) {
			// その休暇区分でない場合
			if (isTheHolidayType(dto, holidayType1, holidayType2) == false) {
				// 次の休暇申請情報へ
				continue;
			}
			// 休暇範囲の日数を加算
			holidayDays += getDays(dto);
		}
		// 休暇日数を取得
		return holidayDays;
	}
	
	/**
	 * 休暇日数を集計する。<br>
	 * @param dtos         休暇申請情報群
	 * @param holidayType1 休暇区分1
	 * @return 休暇日数
	 */
	protected static float totalHolidayDays(Collection<HolidayRequestDtoInterface> dtos, int holidayType1) {
		// 休暇日数を取得
		return totalHolidayDays(dtos, holidayType1, "");
	}
	
	/**
	 * 代休申請情報がその代休種別であるかを確認する。<br>
	 * @param dto            代休申請情報
	 * @param subHolidayType 代休種別
	 * @return 確認結果(true：その代休種別である、false：その代休種別でない)
	 */
	protected static final boolean isTheSubHolidayType(SubHolidayRequestDtoInterface dto, int subHolidayType) {
		// 休暇申請情報が存在しない場合
		if (dto == null) {
			// その代休種別でないと判断
			return false;
		}
		// 代休申請情報がその代休種別であるかを確認
		return dto.getWorkDateSubHolidayType() == subHolidayType;
	}
	
	/**
	 * 代休日数(法定+所定+深夜)を集計する。<br>
	 * @param dtos 代休申請情報群
	 * @return 代休日数
	 */
	public static float totalSubHolidayDays(Collection<SubHolidayRequestDtoInterface> dtos) {
		// 代休日数を集計
		return totalSubHolidayDays(dtos, null);
	}
	
	/**
	 * 法定代休日数を集計する。<br>
	 * @param dtos 代休申請情報群
	 * @return 法定代休日数
	 */
	public static float totalLegalSubHolidayDays(Collection<SubHolidayRequestDtoInterface> dtos) {
		// 法定代休日数を集計
		return totalSubHolidayDays(dtos, TimeConst.CODE_LEGAL_SUBHOLIDAY_CODE);
	}
	
	/**
	 * 所定代休日数を集計する。<br>
	 * @param dtos 代休申請情報群
	 * @return 所定代休日数
	 */
	public static float totalPrescribedSubHolidayDays(Collection<SubHolidayRequestDtoInterface> dtos) {
		// 所定代休日数を集計
		return totalSubHolidayDays(dtos, TimeConst.CODE_PRESCRIBED_SUBHOLIDAY_CODE);
	}
	
	/**
	 * 深夜代休日数を集計する。<br>
	 * @param dtos 代休申請情報群
	 * @return 深夜代休日数
	 */
	public static float totalNightSubHolidayDays(Collection<SubHolidayRequestDtoInterface> dtos) {
		// 深夜代休日数を集計
		return totalSubHolidayDays(dtos, TimeConst.CODE_MIDNIGHT_SUBHOLIDAY_CODE);
	}
	
	/**
	 * 代休日数を集計する。<br>
	 * @param dtos           代休申請情報群
	 * @param subHolidayType 代休種別
	 * @return 代休日数
	 */
	protected static float totalSubHolidayDays(Collection<SubHolidayRequestDtoInterface> dtos, Integer subHolidayType) {
		// 代休日数を準備
		float subHolidayDays = 0F;
		// 代休申請情報毎に処理
		for (SubHolidayRequestDtoInterface dto : dtos) {
			// その代休種別でない場合
			if (subHolidayType != null && isTheSubHolidayType(dto, subHolidayType) == false) {
				// 次の代休申請情報へ
				continue;
			}
			// 休暇範囲の日数を加算
			subHolidayDays += getDays(dto);
		}
		// 代休日数を取得
		return subHolidayDays;
	}
	
}
