import { getQueryClient } from "@/core/libs/tanstack-query";
import { apiService } from "@/core/services/api";
import { ErrorResponse } from "@/core/services/api/types/error-response";
import { PriceAlertDto } from "@/price-alerts/dtos/price-alert-dto";
import { PriceAlertEntity } from "@/price-alerts/entities/price-alert-entity";
import { priceAlertsQueryKey } from "@/price-alerts/hooks/query-options/price-alerts-query-key";
import { useMutation } from "@tanstack/react-query";
import { AxiosError } from "axios";

const updatePriceAlert = async (
  accessToken: string,
  priceAlertId: string,
  priceAlertDto: PriceAlertDto,
) => {
  const { data } = await apiService.put<PriceAlertEntity>(
    `/v1/price-alerts/${priceAlertId}`,
    priceAlertDto,
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    },
  );
  return data;
};

export const useUpdatePriceAlert = (
  accessToken: string,
  priceAlertId: string,
) =>
  useMutation<PriceAlertEntity, AxiosError<ErrorResponse>, PriceAlertDto>({
    mutationKey: [...priceAlertsQueryKey, priceAlertId, "update"] as const,
    mutationFn: (priceAlertDto) =>
      updatePriceAlert(accessToken, priceAlertId, priceAlertDto),
    onSuccess: () => {
      const queryClient = getQueryClient();
      const queryKey = priceAlertsQueryKey;

      queryClient.invalidateQueries({ queryKey });
    },
  });
