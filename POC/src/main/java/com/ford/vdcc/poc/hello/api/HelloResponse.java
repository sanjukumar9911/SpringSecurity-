package com.ford.vdcc.poc.hello.api;

import com.ford.cloudnative.base.api.BaseBodyResponse;
import com.ford.vdcc.poc.hello.api.HelloResponse.HelloResponseResult;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class HelloResponse extends BaseBodyResponse<HelloResponseResult> {

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class HelloResponseResult {
		String greeting;
	}

}
