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
package jp.mosp.time.bean.impl;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import jp.mosp.framework.base.MospException;
import jp.mosp.framework.base.MospParams;
import jp.mosp.framework.constant.MospConst;
import jp.mosp.framework.utils.DateUtility;
import jp.mosp.framework.utils.MospUtility;
import jp.mosp.orangesignal.OrangeSignalUtility;
import jp.mosp.platform.bean.human.HumanSearchBeanInterface;
import jp.mosp.platform.bean.system.SectionReferenceBeanInterface;
import jp.mosp.platform.constant.PlatformConst;
import jp.mosp.platform.constant.PlatformFileConst;
import jp.mosp.platform.constant.PlatformMessageConst;
import jp.mosp.platform.dao.file.ExportDaoInterface;
import jp.mosp.platform.dao.file.ExportFieldDaoInterface;
import jp.mosp.platform.dto.file.ExportDtoInterface;
import jp.mosp.platform.dto.file.ExportFieldDtoInterface;
import jp.mosp.platform.dto.human.HumanDtoInterface;
import jp.mosp.time.base.TimeApplicationBean;
import jp.mosp.time.bean.CutoffUtilBeanInterface;
import jp.mosp.time.bean.HolidayRequestReferenceBeanInterface;
import jp.mosp.time.bean.PaidHolidayDataReferenceBeanInterface;
import jp.mosp.time.bean.UsedPaidHolidayDataExportBeanInterface;
import jp.mosp.time.constant.TimeConst;
import jp.mosp.time.constant.TimeFileConst;
import jp.mosp.time.dto.settings.HolidayRequestDtoInterface;
import jp.mosp.time.dto.settings.PaidHolidayDataDtoInterface;

/**
 * 有給休暇取得状況データエクスポートクラス。<br>
 */
public class UsedPaidHolidayDataExportBean extends TimeApplicationBean
		implements UsedPaidHolidayDataExportBeanInterface {
	
	/**
	 * 拡張子(.csv)
	 */
	public static final String						FILENAME_EXTENSION_CSV	= ".csv";
	
	/**
	 * エクスポートマスタDAO。<br>
	 */
	protected ExportDaoInterface					exportDao;
	
	/**
	 * 人事マスタ検索クラス。<br>
	 */
	protected HumanSearchBeanInterface				humanSearch;
	
	/**
	 * 所属マスタ参照クラス。<br>
	 */
	protected SectionReferenceBeanInterface			sectionReference;
	
	/**
	 * 有給休暇データ
	 */
	protected PaidHolidayDataReferenceBeanInterface	paidHolidayReference;
	
	/**
	 * エクスポートフィールド情報DAO。<br>
	 */
	protected ExportFieldDaoInterface				exportFieldDao;
	
	/**
	 * 締日ユーティリティクラス。<br>
	 */
	protected CutoffUtilBeanInterface				cutoffUtil;
	
	/**
	 * 休暇申請データ参照クラス。<br>
	 */
	protected HolidayRequestReferenceBeanInterface	holidayReference;
	
	/**
	 * 下位所属含むチェックボックス。
	 */
	private int										ckbNeedLowerSection		= 0;
	
	/**
	 * 人事マスタ情報リスト。<br>
	 */
	protected List<HumanDtoInterface>				humanList;
	
	
	/**
	 * {@link TimeApplicationBean#TimeApplicationBean()}を実行する。<br>
	 */
	public UsedPaidHolidayDataExportBean() {
		super();
	}
	
	/**
	 * {@link TimeApplicationBean#TimeApplicationBean(MospParams, Connection)}を実行する。<br>
	 * @param mospParams MosP処理情報
	 * @param connection DBコネクション
	 */
	public UsedPaidHolidayDataExportBean(MospParams mospParams, Connection connection) {
		super(mospParams, connection);
	}
	
	@Override
	public void initBean() throws MospException {
		super.initBean();
		exportDao = (ExportDaoInterface)createDao(ExportDaoInterface.class);
		exportFieldDao = (ExportFieldDaoInterface)createDao(ExportFieldDaoInterface.class);
		humanSearch = (HumanSearchBeanInterface)createBean(HumanSearchBeanInterface.class);
		sectionReference = (SectionReferenceBeanInterface)createBean(SectionReferenceBeanInterface.class);
		cutoffUtil = (CutoffUtilBeanInterface)createBean(CutoffUtilBeanInterface.class);
		holidayReference = (HolidayRequestReferenceBeanInterface)createBean(HolidayRequestReferenceBeanInterface.class);
		paidHolidayReference = (PaidHolidayDataReferenceBeanInterface)createBean(
				PaidHolidayDataReferenceBeanInterface.class);
	}
	
	@Override
	public void export(String exportCode, int startYear, int startMonth, int endYear, int endMonth, String cutoffCode,
			String workPlaceCode, String employmentContractCode, String sectionCode, int ckbNeedLowerSection,
			String positionCode) throws MospException {
		// 下位所属含むチェックボックスの設定
		this.ckbNeedLowerSection = ckbNeedLowerSection;
		ExportDtoInterface dto = exportDao.findForKey(exportCode);
		if (dto == null) {
			// 該当するエクスポート情報が存在しない場合
			addNoExportDataMessage();
			return;
		}
		// 締め期間初日・最終日を取得
		Date startDate = cutoffUtil.getCutoffFirstDate(cutoffCode, startYear, startMonth);
		Date endDate = cutoffUtil.getCutoffLastDate(cutoffCode, endYear, endMonth);
		// CSVデータリストを作成
		List<String[]> list = getCsvDataList(dto, startDate, endDate, cutoffCode, workPlaceCode, employmentContractCode,
				sectionCode, positionCode);
		if (list.isEmpty()) {
			// 該当するエクスポート情報が存在しない場合
			addNoExportDataMessage();
			return;
		}
		// CSVデータリストをMosP処理情報に設定
		mospParams.setFile(OrangeSignalUtility.getOrangeSignalParams(list));
		// ファイル名設定
		mospParams.setFileName(getFilename(dto, startDate, endDate));
	}
	
	/**
	 * 検索条件からCSVデータリストを作成する。
	 * @param dto エクスポートマスタDTO
	 * @param startDate 開始日
	 * @param endDate 終了日
	 * @param cutoffCode 締日コード
	 * @param workPlaceCode 勤務地コード
	 * @param employmentContractCode 雇用契約コード
	 * @param sectionCode 所属コード
	 * @param positionCode 職位コード
	 * @return CSVデータリスト
	 * @throws MospException インスタンスの取得、或いはSQL実行に失敗した場合
	 */
	protected List<String[]> getCsvDataList(ExportDtoInterface dto, Date startDate, Date endDate, String cutoffCode,
			String workPlaceCode, String employmentContractCode, String sectionCode, String positionCode)
			throws MospException {
		// CSVデータリスト準備
		List<String[]> csvDataList = new ArrayList<String[]>();
		// エクスポートフィールド情報取得
		List<ExportFieldDtoInterface> fieldList = exportFieldDao.findForList(dto.getExportCode());
		// ヘッダの有無を確認
		if (dto.getHeader() != PlatformFileConst.HEADER_TYPE_NONE) {
			// ヘッダ情報付加
			addHeader(csvDataList, getHeader(dto, fieldList));
		}
		// 人事情報リスト取得
		getHumanList(startDate, endDate, cutoffCode, workPlaceCode, employmentContractCode, sectionCode, positionCode);
		// 全社員の期間中に有効な有給休暇データを使用した休暇申請マップ
		Map<String, Map<Date, List<HolidayRequestDtoInterface>>> requestMap = getHolidayRequestMap(startDate, endDate);
		// 社員毎に処理
		for (HumanDtoInterface humanDto : humanList) {
			getUsedPaidHolidayData(csvDataList, fieldList, humanDto, requestMap, startDate, endDate);
		}
		return csvDataList;
	}
	
	/**
	 * 検索条件に基づき人事情報を検索し、CSVデータリストに付加する。<br>
	 * @param startDate 開始日
	 * @param endDate 終了日
	 * @param cutoffCode 締日コード
	 * @param workPlaceCode 勤務地コード
	 * @param employmentContractCode 雇用契約コード
	 * @param sectionCode 所属コード
	 * @param positionCode 職位コード
	 * @throws MospException インスタンスの取得、或いはSQL実行に失敗した場合
	 */
	protected void getHumanList(Date startDate, Date endDate, String cutoffCode, String workPlaceCode,
			String employmentContractCode, String sectionCode, String positionCode) throws MospException {
		// 人事情報検索条件設定(在職)
		humanSearch.setStartDate(startDate);
		humanSearch.setEndDate(endDate);
		humanSearch.setTargetDate(endDate);
		humanSearch.setWorkPlaceCode(workPlaceCode);
		humanSearch.setEmploymentContractCode(employmentContractCode);
		humanSearch.setSectionCode(sectionCode);
		humanSearch.setPositionCode(positionCode);
		// 検索条件設定(状態)
		humanSearch.setStateType(PlatformConst.EMPLOYEE_STATE_PRESENCE);
		// 検索条件設定(下位所属要否) 下位所属含むチェックボックスで判定
		if (ckbNeedLowerSection == 1) {
			humanSearch.setNeedLowerSection(true);
		} else {
			humanSearch.setNeedLowerSection(false);
		}
		// 検索条件設定(兼務要否)
		humanSearch.setNeedConcurrent(true);
		// 検索条件設定(操作区分)
		humanSearch.setOperationType(MospConst.OPERATION_TYPE_REFER);
		// 人事情報検索(在職)
		List<HumanDtoInterface> presenceHumanList = humanSearch.search();
		
		// 人事情報検索条件設定(休職)
		// 検索条件設定(状態)
		humanSearch.setStateType(PlatformConst.EMPLOYEE_STATE_SUSPEND);
		// 人事情報検索(休職)
		List<HumanDtoInterface> suspendHumanList = humanSearch.search();
		
		// 人事情報検索(在職+休職)
		List<HumanDtoInterface> allHumanList = new ArrayList<HumanDtoInterface>();
		allHumanList.addAll(presenceHumanList);
		allHumanList.addAll(suspendHumanList);
		
		// 社員リスト準備
		humanList = new ArrayList<HumanDtoInterface>();
		if (cutoffCode.isEmpty()) {
			humanList = allHumanList;
		} else {
			for (HumanDtoInterface humanDto : allHumanList) {
				if (!hasCutoffSettings(humanDto.getPersonalId(), endDate)) {
					continue;
				}
				if (!cutoffDto.getCutoffCode().equals(cutoffCode)) {
					continue;
				}
				humanList.add(humanDto);
			}
		}
	}
	
	/**
	 * CSVデータリストに有給休暇取得状況データをセットする。
	 * @param csvDataList 出力CSVデータリスト
	 * @param fieldList エクスポートフィールドDTOリスト
	 * @param humanDto 人事情報DTO
	 * @param map 対象期間内に有効な休暇データでの休暇申請マップ
	 * @param startDate 締期間開始日
	 * @param endDate 締期間最終日
	 * @throws MospException インスタンスの取得、或いはSQL実行に失敗した場合
	 */
	protected void getUsedPaidHolidayData(List<String[]> csvDataList, List<ExportFieldDtoInterface> fieldList,
			HumanDtoInterface humanDto, Map<String, Map<Date, List<HolidayRequestDtoInterface>>> map, Date startDate,
			Date endDate) throws MospException {
		
		String personalId = humanDto.getPersonalId();
		String sectionName = null;
		String sectionDisplayName = null;
		// 有給休暇取得日をキーとした期間内の有給休暇の休暇申請Mapを取得
		Map<Date, List<HolidayRequestDtoInterface>> holidayMap = map.get(personalId);
		if (holidayMap == null) {
			// 休暇情報がない場合
			return;
		}
		// 有給休暇取得日ごとに処理
		for (Entry<Date, List<HolidayRequestDtoInterface>> entry : holidayMap.entrySet()) {
			Date acquisitionDate = entry.getKey();
			List<HolidayRequestDtoInterface> paidHolidayList = entry.getValue();
			int n = 0;
			double usedDays = 0;
			// 休暇申請データごとに処理
			// データ配列準備
			StringBuffer sb = new StringBuffer();
			String[] fieldValue = new String[fieldList.size()];
			for (HolidayRequestDtoInterface requestDto : paidHolidayList) {
				// 休暇種別が有給休暇か確認
				if (requestDto.getHolidayType1() != TimeConst.CODE_HOLIDAYTYPE_HOLIDAY) {
					// 有給休暇以外の場合
					continue;
				}
				// 時間休か確認
				if (requestDto.getHolidayRange() == TimeConst.CODE_HOLIDAY_RANGE_TIME) {
					// 時間休の場合
					continue;
				}
				// 休暇申請日が期間内に含まれているか確認する。
				if (!DateUtility.isTermContain(requestDto.getRequestStartDate(), startDate, endDate)) {
					continue;
				}
				// 使用日数加算
				usedDays += requestDto.getUseDay();
				// 休暇申請情報ごとに処理
				if (n != 0) {
					// 区切り文字
					sb.append(",");
				}
				sb.append(DateUtility.getStringDate(requestDto.getRequestStartDate()));
				sb.append(MospConst.STR_SB_SPACE);
				
				if (requestDto.getHolidayRange() == TimeConst.CODE_HOLIDAY_RANGE_ALL) {
					// 全休の場合
					sb.append(mospParams.getName("AllTime"));
				}
				if (requestDto.getHolidayRange() == TimeConst.CODE_HOLIDAY_RANGE_AM) {
					// 前半休の場合
					sb.append(mospParams.getName("FrontTime"));
				}
				if (requestDto.getHolidayRange() == TimeConst.CODE_HOLIDAY_RANGE_PM) {
					// 後半休の場合
					sb.append(mospParams.getName("BackTime"));
				}
				n++;
			}
			// フィールド毎に処理
			for (int i = 0; i < fieldList.size(); i++) {
				String fieldName = fieldList.get(i).getFieldName();
				
				// 社員コードの場合
				if (fieldName.equals(PlatformFileConst.FIELD_EMPLOYEE_CODE)) {
					fieldValue[i] = humanDto.getEmployeeCode();
					continue;
				}
				// 氏名の場合
				if (fieldName.equals(PlatformFileConst.FIELD_FULL_NAME)) {
					fieldValue[i] = MospUtility.getHumansName(humanDto.getFirstName(), humanDto.getLastName());
					continue;
				}
				// 取得日の場合
				if (fieldName.equals(TimeFileConst.ACQUISITION_DATE)) {
					fieldValue[i] = DateUtility.getStringDate(acquisitionDate);
					continue;
				}
				// 所属名称の場合
				if (fieldName.equals(PlatformFileConst.FIELD_SECTION_NAME)) {
					if (sectionName == null) {
						sectionName = sectionReference.getSectionName(humanDto.getSectionCode(), endDate);
					}
					fieldValue[i] = sectionName;
					continue;
				}
				// 所属表示名称の場合
				if (fieldName.equals(PlatformFileConst.FIELD_SECTION_DISPLAY)) {
					if (sectionDisplayName == null) {
						sectionDisplayName = sectionReference.getSectionDisplay(humanDto.getSectionCode(), endDate);
					}
					fieldValue[i] = sectionDisplayName;
					continue;
				}
				// 申請日数の場合
				if (fieldName.equals(TimeFileConst.USED_DAYS)) {
					fieldValue[i] = String.valueOf(usedDays);
					continue;
				}
				// 申請日の場合
				if (fieldName.equals(TimeFileConst.APPLIE_DATE)) {
					fieldValue[i] = sb.toString();
					continue;
				}
			}
			csvDataList.add(fieldValue);
		}
	}
	
	/**
	 * 対象期間内に有効な有休休暇データを使用して取得した全社員の休暇申請Mapを取得する。
	 * @param startDate 期間開始日
	 * @param endDate 期間終了日
	 * @return 対象期間内の全社員の休暇申請Map
	 * @throws MospException インスタンスの取得、或いはSQL実行に失敗した場合
	 */
	protected Map<String, Map<Date, List<HolidayRequestDtoInterface>>> getHolidayRequestMap(Date startDate,
			Date endDate) throws MospException {
		// 期間内に有効な有給休暇データを取得
		List<PaidHolidayDataDtoInterface> paidHolidayList = paidHolidayReference
			.findForAllTermOfEffectiveList(startDate, endDate);
		// 返却Map準備
		Map<String, Map<Date, List<HolidayRequestDtoInterface>>> paidHolidayMap = new TreeMap<String, Map<Date, List<HolidayRequestDtoInterface>>>();
		// 期間内の全社員の有給休暇申請を取得
		Map<String, Map<Date, List<HolidayRequestDtoInterface>>> requestMap = getPersonalHolidayRequestMap(startDate,
				endDate);
		// 有給休暇付与データごとに処理
		for (PaidHolidayDataDtoInterface dto : paidHolidayList) {
			String personalId = dto.getPersonalId();
			Date acquisitionDate = dto.getAcquisitionDate();
			// 対象社員の休暇申請Mapを取得
			Map<Date, List<HolidayRequestDtoInterface>> map = requestMap.get(personalId);
			if (map == null) {
				// 存在しない場合、空のMapを用意
				map = new TreeMap<Date, List<HolidayRequestDtoInterface>>();
			}
			// 休暇申請Mapから、対象付与日の申請リストを取得。
			List<HolidayRequestDtoInterface> requestList = map.get(acquisitionDate);
			if (requestList == null) {
				// 存在しない場合、空のリストを用意。
				requestList = new ArrayList<HolidayRequestDtoInterface>();
			}
			// 返却用のMapから対象社員の休暇申請Mapを取得
			Map<Date, List<HolidayRequestDtoInterface>> resultMap = paidHolidayMap.get(personalId);
			if (resultMap == null) {
				// 存在しない場合、新規に用意
				resultMap = new TreeMap<Date, List<HolidayRequestDtoInterface>>();
			}
			// 休暇申請Mapに休暇申請リストをセット
			resultMap.put(acquisitionDate, requestList);
			// 返却用Mapに休暇申請Mapをセット
			paidHolidayMap.put(personalId, resultMap);
		}
		return paidHolidayMap;
	}
	
	/**
	 * 期間内の全社員の未承認以上の有給休暇の休暇申請をMapで取得する。
	 * @param startDate 期間開始日
	 * @param endDate 期間終了日
	 * @return <個人ID,<付与日,休暇申請リスト>>のMap
	 * @throws MospException インスタンスの取得、或いはSQL実行に失敗した場合
	 */
	protected Map<String, Map<Date, List<HolidayRequestDtoInterface>>> getPersonalHolidayRequestMap(Date startDate,
			Date endDate) throws MospException {
		// 返却用Map用意
		Map<String, Map<Date, List<HolidayRequestDtoInterface>>> resultMap = new TreeMap<String, Map<Date, List<HolidayRequestDtoInterface>>>();
		// 対象期間内の全社員の未承認以上の有給休暇申請を取得する。
		List<HolidayRequestDtoInterface> requests = holidayReference.getAppliedPaidHolidayRequests(startDate, endDate);
		// 休暇申請ごとに処理
		for (HolidayRequestDtoInterface dto : requests) {
			String personalId = dto.getPersonalId();
			Date acquisitionDate = dto.getHolidayAcquisitionDate();
			// 返却用Mapに既に存在する、付与日をキーとした休暇申請Mapを個人IDで取得する。
			Map<Date, List<HolidayRequestDtoInterface>> map = resultMap.get(personalId);
			if (map == null) {
				// 返却用Mapに存在しない場合、新規で作成。
				map = new TreeMap<Date, List<HolidayRequestDtoInterface>>();
			}
			// 付与日をキーとした休暇申請Mapから、対象休暇申請の付与日と同じ付与日の休暇申請リストを取得。
			List<HolidayRequestDtoInterface> list = map.get(acquisitionDate);
			if (list == null) {
				// 存在しない場合、新規にリストを作成。
				list = new ArrayList<HolidayRequestDtoInterface>();
			}
			// 対象休暇申請をリストに追加
			list.add(dto);
			// 返却用Mapに追加。
			map.put(acquisitionDate, list);
			resultMap.put(personalId, map);
		}
		return resultMap;
	}
	
	/**
	 * エクスポートデータが存在しない場合のメッセージを設定する。<br>
	 */
	protected void addNoExportDataMessage() {
		String rep = mospParams.getName("Export", "Information");
		mospParams.addErrorMessage(PlatformMessageConst.MSG_NO_ITEM, rep);
	}
	
	/**
	 * 送出ファイル名を取得する。<br>
	 * @param dto 対象DTO
	 * @param firstDate 初日
	 * @param lastDate 末日
	 * @return 送出ファイル名
	 */
	protected String getFilename(ExportDtoInterface dto, Date firstDate, Date lastDate) {
		StringBuffer sb = new StringBuffer();
		// エクスポートコード
		sb.append(dto.getExportCode());
		// ハイフン
		sb.append(mospParams.getName("Hyphen"));
		// 開始年
		sb.append(DateUtility.getStringYear(firstDate));
		// 開始月
		sb.append(DateUtility.getStringMonth(firstDate));
		// 開始日
		sb.append(DateUtility.getStringDay(firstDate));
		// ハイフン
		sb.append(mospParams.getName("Hyphen"));
		// 終了年
		sb.append(DateUtility.getStringYear(lastDate));
		// 終了月
		sb.append(DateUtility.getStringMonth(lastDate));
		// 終了日
		sb.append(DateUtility.getStringDay(lastDate));
		// 拡張子
		sb.append(getFilenameExtension(dto));
		return sb.toString();
	}
	
	/**
	 * 拡張子を取得する。<br>
	 * @param dto 対象DTO
	 * @return 拡張子
	 */
	protected String getFilenameExtension(ExportDtoInterface dto) {
		if (PlatformFileConst.FILE_TYPE_CSV.equals(dto.getType())) {
			// CSV
			return FILENAME_EXTENSION_CSV;
		}
		return "";
	}
	
	/**
	 * CSVデータリストにヘッダを付加する。<br>
	 * @param csvDataList CSVデータリスト
	 * @param header ヘッダ
	 */
	protected void addHeader(List<String[]> csvDataList, String[] header) {
		csvDataList.add(0, header);
	}
	
	/**
	 * ヘッダを取得する。<br>
	 * @param dto 対象DTO
	 * @param list フィールドDTOリスト
	 * @return ヘッダ
	 */
	protected String[] getHeader(ExportDtoInterface dto, List<ExportFieldDtoInterface> list) {
		String[][] array = mospParams.getProperties().getCodeArray(dto.getExportTable(), false);
		String[] header = new String[list.size()];
		for (int i = 0; i < header.length; i++) {
			header[i] = MospUtility.getCodeName(list.get(i).getFieldName(), array);
		}
		return header;
	}
}
