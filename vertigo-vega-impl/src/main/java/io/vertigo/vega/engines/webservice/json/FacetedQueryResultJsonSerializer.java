package io.vertigo.vega.engines.webservice.json;

import io.vertigo.dynamo.collections.model.Facet;
import io.vertigo.dynamo.collections.model.FacetValue;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.model.DtList;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * JsonSerializer of FacetedQueryResult.
 * @author npiedeloup
 */
final class FacetedQueryResultJsonSerializer implements JsonSerializer<FacetedQueryResult<?, ?>> {

	/** {@inheritDoc} */
	@Override
	public JsonElement serialize(final FacetedQueryResult<?, ?> facetedQueryResult, final Type typeOfSrc, final JsonSerializationContext context) {
		final JsonObject jsonObject = new JsonObject();

		//1- add result list as data
		if (facetedQueryResult.getClusters().isEmpty()) {
			final JsonArray jsonList = (JsonArray) context.serialize(facetedQueryResult.getDtList());
			jsonObject.add("list", jsonList);
		} else {
			//if it's a cluster add data's cluster
			final JsonArray jsonCluster = new JsonArray();
			for (final Entry<FacetValue, ?> cluster : facetedQueryResult.getClusters().entrySet()) {
				final JsonArray jsonList = (JsonArray) context.serialize(cluster.getValue());
				final JsonObject jsonClusterElement = new JsonObject();
				jsonClusterElement.add(cluster.getKey().getLabel().getDisplay(), jsonList);
				jsonCluster.add(jsonClusterElement);
			}
			jsonObject.add("groups", jsonCluster);
		}

		//2- add facet list as facets
		final List<Facet> facets = facetedQueryResult.getFacets();
		final JsonArray jsonFacet = new JsonArray();
		for (final Facet facet : facets) {
			final JsonArray jsonFacetValues = new JsonArray();
			for (final Entry<FacetValue, Long> entry : facet.getFacetValues().entrySet()) {
				final JsonObject jsonFacetValuesElement = new JsonObject();
				jsonFacetValuesElement.addProperty(entry.getKey().getLabel().getDisplay(), entry.getValue());
				jsonFacetValues.add(jsonFacetValuesElement);
			}
			final String facetName = facet.getDefinition().getName();
			final JsonObject jsonFacetElement = new JsonObject();
			jsonFacetElement.add(facetName, jsonFacetValues);
			jsonFacet.add(jsonFacetElement);
		}
		jsonObject.add("facets", jsonFacet);

		//3 -add totalCount
		jsonObject.addProperty(DtList.TOTAL_COUNT_META, facetedQueryResult.getCount());
		return jsonObject;
	}
}