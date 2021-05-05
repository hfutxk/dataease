package io.dataease.service.dataset.impl.direct;

import io.dataease.base.domain.DatasetTable;
import io.dataease.base.domain.DatasetTableField;
import io.dataease.base.domain.Datasource;
import io.dataease.datasource.provider.DatasourceProvider;
import io.dataease.datasource.provider.ProviderFactory;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.datasource.service.DatasourceService;
import io.dataease.service.dataset.DataSetFieldService;
import io.dataease.service.dataset.DataSetTableFieldsService;
import io.dataease.service.dataset.DataSetTableService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service("directDataSetFieldService")
public class DirectFieldService implements DataSetFieldService {


    @Resource
    private DataSetTableFieldsService dataSetTableFieldsService;

    @Resource
    private DataSetTableService dataSetTableService;


    @Resource
    private DatasourceService datasourceService;

    @Override
    public List<Object> fieldValues(String fieldId) {


        List<DatasetTableField> list = dataSetTableFieldsService.getListByIds(new ArrayList<String>() {{
            add(fieldId);
        }});
        if (CollectionUtils.isEmpty(list)) return null;

        DatasetTableField field = list.get(0);
        String tableId = field.getTableId();
        if (StringUtils.isEmpty(tableId))return null;
        DatasetTable datasetTable = dataSetTableService.get(tableId);
        if (ObjectUtils.isEmpty(datasetTable) || StringUtils.isEmpty(datasetTable.getName())) return null;
        String tableName = datasetTable.getName();

        String dataSourceId = datasetTable.getDataSourceId();
        if( StringUtils.isEmpty(dataSourceId)) return null;
        Datasource ds = datasourceService.get(dataSourceId);
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        String querySQL = dataSetTableService.createQuerySQL(ds.getType(), tableName, new String[]{field.getOriginName()});
        datasourceRequest.setQuery(querySQL);
        try {
            List<String[]> rows = datasourceProvider.getData(datasourceRequest);
            List<Object> results = rows.stream().map(row -> row[0]).distinct().collect(Collectors.toList());
            return results;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}