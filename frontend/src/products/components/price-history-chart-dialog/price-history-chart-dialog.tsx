"use client";

import { Button } from "@/core/components/ui/button";
import {
  ChartConfig,
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
} from "@/core/components/ui/chart";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/core/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/core/components/ui/select";
import { Skeleton } from "@/core/components/ui/skeleton";
import { formatCurrency } from "@/core/utils/format-currency";
import { formatDate } from "@/core/utils/format-date";
import { ShopEntity } from "@/products/entities/shop-entity";
import { useFetchProduct } from "@/products/hooks/client/use-fetch-product";
import { convertPriceDataToChartFormat } from "@/products/utils/convert-price-data-to-chart-format";
import { ChartNoAxesCombined } from "lucide-react";
import { ReactNode, useMemo, useState } from "react";
import { Area, AreaChart, CartesianGrid, XAxis, YAxis } from "recharts";
import { AxisDomain } from "recharts/types/util/types";

interface PriceHistoryChartDialogProps {
  readonly productId: string;
}

type TimeRangeFilterValue = "7" | "30" | "90" | "180";

const chartConfig: Record<ShopEntity, { label: ReactNode; color: string }> = {
  "Media Expert": {
    label: "Media Expert",
    color: "var(--media-expert-brand)",
  },
  "Morele.net": {
    label: "Morele.net",
    color: "var(--morele-brand)",
  },
  "RTV Euro AGD": {
    label: "RTV Euro AGD",
    color: "var(--rtv-euro-agd-brand)",
  },
} satisfies ChartConfig;

const domain: AxisDomain = ([dataMin, dataMax]) =>
  [Math.floor(dataMin / 25) * 25, Math.ceil(dataMax / 25) * 25] as const;

export const PriceHistoryChartDialog = ({
  productId,
}: PriceHistoryChartDialogProps) => {
  const [isOpen, setIsOpen] = useState(false);
  const [isChartVisible, setIsChartVisible] = useState(false);
  const [timeRangeFilter, setTimeRangeFilter] =
    useState<TimeRangeFilterValue>("7");

  const { data: product, isLoading } = useFetchProduct(productId, {
    priceStampRangeDays: timeRangeFilter,
  });

  const chartData = useMemo(
    () => convertPriceDataToChartFormat(product?.offers ?? []),
    [product?.offers],
  );
  const shopKeys = Object.keys(chartConfig) as (keyof typeof chartConfig)[];

  const handleOpenChange = (newIsOpen: boolean) => {
    setIsOpen(newIsOpen);

    // render chart after dialog is opened
    if (newIsOpen) setTimeout(() => setIsChartVisible(true), 0);
    else setIsChartVisible(false);
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleOpenChange}>
      <DialogTrigger asChild>
        <Button variant="invisible" size="noPadding">
          <ChartNoAxesCombined />
          <span>POKAŻ HISTORIĘ CEN</span>
        </Button>
      </DialogTrigger>

      <DialogContent className="min-w-full lg:min-w-3/4">
        <DialogHeader className="flex items-center justify-between gap-3 py-4 sm:flex-row sm:items-start md:py-6">
          <div className="w-3/5 space-y-2 text-center sm:text-left">
            <DialogTitle>Historia cen</DialogTitle>
            <DialogDescription>
              Na wykresie możesz łatwo prześledzić zmiany cen tego produktu w
              czasie.
            </DialogDescription>
          </div>

          <Select
            value={timeRangeFilter}
            onValueChange={(value) =>
              setTimeRangeFilter(value as TimeRangeFilterValue)
            }
          >
            <SelectTrigger className="w-45">
              <SelectValue placeholder="Ostatnie 7 dni" />
            </SelectTrigger>
            <SelectContent className="rounded-xl">
              <SelectItem value="7">Ostatnie 7 dni</SelectItem>
              <SelectItem value="30">Ostatnie 30 dni</SelectItem>
              <SelectItem value="90" className="hidden sm:block">
                Ostatnie 3 miesiące
              </SelectItem>
              <SelectItem value="180" className="hidden sm:block">
                Ostatnie pół roku
              </SelectItem>
            </SelectContent>
          </Select>
        </DialogHeader>

        {isLoading && (
          <Skeleton className="aspect-auto h-96 w-full sm:h-120 xl:h-144" />
        )}

        {!isLoading && (
          <ChartContainer
            config={chartConfig}
            className="aspect-auto h-96 w-full sm:h-120 xl:h-144"
          >
            <AreaChart data={chartData}>
              <defs>
                {shopKeys.map((shopKey) => (
                  <linearGradient
                    key={`fillDef${shopKey}`}
                    id={`fill${shopKey.replace(/\s+/g, "")}`}
                    x1="0"
                    y1="0"
                    x2="0"
                    y2="1"
                  >
                    <stop
                      offset="5%"
                      stopColor={chartConfig[shopKey]?.color}
                      stopOpacity={0.8}
                    />
                    <stop
                      offset="95%"
                      stopColor={chartConfig[shopKey]?.color}
                      stopOpacity={0.1}
                    />
                  </linearGradient>
                ))}
              </defs>
              <CartesianGrid vertical={false} />
              <YAxis
                tickCount={6}
                tickLine={false}
                axisLine={false}
                allowDataOverflow={false}
                tickMargin={8}
                tickFormatter={(value) => `${value} zł`}
                domain={domain}
              />
              <XAxis
                dataKey="date"
                tickLine={false}
                axisLine={false}
                interval="preserveStartEnd"
                allowDataOverflow={false}
                tickMargin={8}
                tickFormatter={(value) =>
                  formatDate(value, {
                    month: "short",
                    day: "numeric",
                  })
                }
              />
              <ChartTooltip
                cursor={true}
                content={
                  <ChartTooltipContent
                    indicator="dot"
                    labelFormatter={(value) =>
                      formatDate(value, {
                        month: "long",
                        day: "numeric",
                      })
                    }
                    valueFormatter={(value) =>
                      formatCurrency(Number(value.replace(/,/g, ".")), "PLN")
                    }
                  />
                }
              />
              {isChartVisible &&
                shopKeys.map((shopKey) => (
                  <Area
                    key={shopKey}
                    dataKey={shopKey}
                    type="linear"
                    fill={`url(#fill${shopKey.replace(/\s+/g, "")})`}
                    stroke={chartConfig[shopKey]?.color}
                    strokeWidth={2}
                    activeDot={{ r: 5 }}
                    dot
                  />
                ))}
              <ChartLegend content={<ChartLegendContent />} />
            </AreaChart>
          </ChartContainer>
        )}
      </DialogContent>
    </Dialog>
  );
};
