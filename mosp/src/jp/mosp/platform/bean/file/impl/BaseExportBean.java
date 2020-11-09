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
package jp.mosp.platform.bean.file.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jp.mosp.framework.base.MospException;
import jp.mosp.framework.constant.MospConst;
import jp.mosp.framework.utils.DateUtility;
import jp.mosp.framework.utils.MospUtility;
import jp.mosp.orangesignal.OrangeSignalUtility;
import jp.mosp.platform.base.PlatformBean;
import jp.mosp.platform.bean.file.ExportBeanInterface;
import jp.mosp.platform.bean.file.ExportFieldReferenceBeanInterface;
import jp.mosp.platform.bean.human.HumanSearchBeanInterface;
import jp.mosp.platform.constant.PlatformConst;
import jp.mosp.platform.constant.PlatformFileConst;
import jp.mosp.platform.constant.PlatformMessageConst;
import jp.mosp.platform.dao.file.ExportDaoInterface;
import jp.mosp.platform.dto.file.ExportDtoInterface;
import jp.mosp.platform.dto.human.HumanDtoInterface;
import jp.mosp.platform.utils.PlatformNamingUtility;
import jp.mosp.platform.utils.PlatformUtility;

/**
 * エクスポート処理。<br>
 * エクスポート処理で必要となる機能を提供する。<br>
 */
public abstract class BaseExportBean extends PlatformBean implements ExportBeanInterface {
	
	/**
	 * 拡張子(エクスポートファイル)。<br>
	 */
	protected static final String				EXT_EXPORT_FILE	= ".csv";
	
	/**
	 * エクスポートマスタDAO。<br>
	 */
	protected ExportDaoInterface				exportDao;
	
	/**
	 * エクスポートフィールド参照処理。<br>
	 */
	protected ExportFieldReferenceBeanInterface	exportFieldRefer;
	
	/**
	 * 人事情報検索処理。<br>
	 */
	protected HumanSearchBeanInterface			humanSearch;
	
	
	/**
	 * {@link PlatformBean#PlatformBean()}を実行する。<br>
	 */
	public BaseExportBean() {
		super();
	}
	
	@Override
	public void initBean() throws MospException {
		// DAO及びBeanを準備
		exportDao = (ExportDaoInterface)createDao(ExportDaoInterface.class);
		exportFieldRefer = createBeanInstance(ExportFieldReferenceBeanInterface.class);
		humanSearch = createBeanInstance(HumanSearchBeanInterface.class);
	}
	
	@Override
	public void export(String exportCode, Date targetDate, String workPlaceCode, String employmentContractCode,
			String sectionCode, String positionCode) throws MospException {
		// エクスポートマスタ情報を取得
		ExportDtoInterface exportDto = exportDao.findForKey(exportCode);
		// エクスポートマスタ情報が取得できなかった場合
		if (exportDto == null) {
			// エラーメッセージを設定
			addNoExportDataMessage();
			return;
		}
		// エクスポートフィールド名称リスト(フィールド順序昇順)を取得
		List<String> fieldList = exportFieldRefer.getExportFieldNameList(exportCode);
		// CSVデータリストを作成
		List<String[]> csvDataList = makeCsvDataList(fieldList, targetDate, workPlaceCode, employmentContractCode,
				sectionCode, positionCode);
		// CSVデータリスト確認
		if (csvDataList.isEmpty()) {
			// 該当するエクスポート情報が存在しない場合
			addNoExportDataMessage();
			return;
		}
		// CSVデータリストにヘッダを付加
		addHeader(csvDataList, exportDto, fieldList);
		// CSVデータリストをMosP処理情報に設定
		mospParams.setFile(OrangeSignalUtility.getOrangeSignalParams(csvDataList));
		// 送出ファイル名をMosP処理情報に設定
		setFileName(exportCode, targetDate);
	}
	
	/**
	 * CSVデータリストを作成する。<br>
	 * <br>
	 * 各エクスポート処理で実装する。<br>
	 * <br>
	 * @param fieldList              エクスポートフィールド名称リスト(フィールド順序昇順)
	 * @param targetDate             対象日
	 * @param workPlaceCode          勤務地コード
	 * @param employmentContractCode 雇用契約コード
	 * @param sectionCode            所属コード
	 * @param positionCode           職位コード
	 * @return CSVデータリスト
	 * @throws MospException インスタンスの取得或いはSQL実行に失敗した場合
	 */
	protected List<String[]> makeCsvDataList(List<String> fieldList, Date targetDate, String workPlaceCode,
			String employmentContractCode, String sectionCode, String positionCode) throws MospException {
		// 処理無し(各エクスポート処理で実装)
		return Collections.emptyList();
	}
	
	/**
	 * CSVデータリストにヘッダを付加する。<br>
	 * @param csvDataList CSVデータリスト
	 * @param exportDto   エクスポート情報
	 * @param fieldList   エクスポートフィールド名称リスト(フィールド順序昇順)
	 */
	protected void addHeader(List<String[]> csvDataList, ExportDtoInterface exportDto, List<String> fieldList) {
		// エクスポート情報のヘッダ有無が無である場合
		if (MospUtility.isEqual(exportDto.getHeader(), PlatformFileConst.HEADER_TYPE_NONE)) {
			// 処理無し
			return;
		}
		// コードキー(エクスポートフィールド)を準備(データ区分と同じ)
		String codeKey = exportDto.getExportTable();
		// ヘッダ準備
		String[] header = new String[fieldList.size()];
		// インデックス準備
		int i = 0;
		// フィールド毎にヘッダを付加
		for (String field : fieldList) {
			header[i++] = getCodeName(field, codeKey);
		}
		// ヘッダをCSVリストに追加
		csvDataList.add(0, header);
	}
	
	/**
	 * 検索条件に基づき人事情報を検索する。<br>
	 * @param targetDate             対象日
	 * @param workPlaceCode          勤務地コード
	 * @param employmentContractCode 雇用契約コード
	 * @param sectionCode            所属コード
	 * @param positionCode           職位コード
	 * @return 人事情報リスト
	 * @throws MospException インスタンスの取得或いはSQL実行に失敗した場合
	 */
	protected List<HumanDtoInterface> searchHumanList(Date targetDate, String workPlaceCode,
			String employmentContractCode, String sectionCode, String positionCode) throws MospException {
		// 人事情報検索条件設定
		humanSearch.setTargetDate(targetDate);
		humanSearch.setWorkPlaceCode(workPlaceCode);
		humanSearch.setEmploymentContractCode(employmentContractCode);
		humanSearch.setSectionCode(sectionCode);
		humanSearch.setPositionCode(positionCode);
		// 検索条件設定(状態)
		humanSearch.setStateType(PlatformConst.EMPLOYEE_STATE_PRESENCE);
		// 検索条件設定(下位所属要否)
		humanSearch.setNeedLowerSection(true);
		// 検索条件設定(兼務要否)
		humanSearch.setNeedConcurrent(true);
		// 検索条件設定(操作区分)
		humanSearch.setOperationType(MospConst.OPERATION_TYPE_REFER);
		// 人事情報検索
		return humanSearch.search();
	}
	
	/**
	 * 検索条件に基づき人事情報を検索する。<br>
	 * @param targetDate             対象日
	 * @param workPlaceCode          勤務地コード
	 * @param employmentContractCode 雇用契約コード
	 * @param sectionCode            所属コード
	 * @param positionCode           職位コード
	 * @return 人事情報群(キー：個人ID)
	 * @throws MospException インスタンスの取得或いはSQL実行に失敗した場合
	 */
	protected Map<String, HumanDtoInterface> searchHumanData(Date targetDate, String workPlaceCode,
			String employmentContractCode, String sectionCode, String positionCode) throws MospException {
		// 人事情報検索
		return PlatformUtility.getPersonalIdDtoMap(
				searchHumanList(targetDate, workPlaceCode, employmentContractCode, sectionCode, positionCode));
	}
	
	/**
	 * 送出ファイル名をMosP処理情報に設定する。<br>
	 * @param exportCode エクスポートコード
	 * @param targetDate 対象日
	 */
	protected void setFileName(String exportCode, Date targetDate) {
		// 送出ファイル名を作成
		StringBuilder sb = new StringBuilder(exportCode);
		sb.append(PlatformNamingUtility.hyphen(mospParams));
		sb.append(DateUtility.getStringDateNoSeparator(targetDate));
		sb.append(EXT_EXPORT_FILE);
		// 送出ファイル名設定
		mospParams.setFileName(sb.toString());
	}
	
	/**
	 * エクスポートデータが存在しない場合のメッセージを設定する。<br>
	 */
	protected void addNoExportDataMessage() {
		String rep = mospParams.getName("Export", "Information");
		mospParams.addErrorMessage(PlatformMessageConst.MSG_NO_ITEM, rep);
	}
	
}
