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
import { formatCurrency } from "@/core/utils/format-currency";
import { formatDate } from "@/core/utils/format-date";
import { OfferEntity } from "@/products/entities/offer-entity";
import { convertPriceDataToChartFormat } from "@/products/utils/convert-price-data-to-chart-format";
import { ChartNoAxesCombined } from "lucide-react";
import { useMemo, useState } from "react";
import { Area, AreaChart, CartesianGrid, XAxis, YAxis } from "recharts";

interface PriceHistoryChartDialogProps {
  readonly offers: OfferEntity[];
}

type TimeRangeFilterValue = "7" | "30" | "90" | "180";

const chartConfig = {
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

export const PriceHistoryChartDialog = ({
  offers,
}: PriceHistoryChartDialogProps) => {
  const [isOpen, setIsOpen] = useState(false);
  const [isChartVisible, setIsChartVisible] = useState(false);
  const [timeRangeFilter, setTimeRangeFilter] =
    useState<TimeRangeFilterValue>("7");

  const shopKeys = Object.keys(chartConfig) as (keyof typeof chartConfig)[];
  const chartData = useMemo(
    () => convertPriceDataToChartFormat(offers),
    [offers],
  );
  // TODO: implement filtering
  const filteredChartData = useMemo(() => chartData, [chartData]);

  const handleOpenChange = (newIsOpen: boolean) => {
    setIsOpen(newIsOpen);

    if (newIsOpen) setTimeout(() => setIsChartVisible(true), 0);
    else setIsChartVisible(false);
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleOpenChange}>
      <DialogTrigger asChild>
        <Button variant="invisible" size="noPadding">
          <ChartNoAxesCombined />
          <span>Pokaż historię cen</span>
        </Button>
      </DialogTrigger>

      <DialogContent className="min-w-3/4">
        <DialogHeader className="flex items-center justify-between gap-2 py-4 sm:flex-row sm:items-start">
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
              <SelectItem value="7" className="rounded-lg">
                Ostatnie 7 dni
              </SelectItem>
              <SelectItem value="30" className="rounded-lg">
                Ostatnie 30 dni
              </SelectItem>
              <SelectItem value="90" className="rounded-lg">
                Ostatnie 3 miesiące
              </SelectItem>
              <SelectItem value="180" className="rounded-lg">
                Ostatnie pół roku
              </SelectItem>
            </SelectContent>
          </Select>
        </DialogHeader>

        <ChartContainer
          config={chartConfig}
          className="aspect-auto h-96 w-full"
        >
          <AreaChart data={filteredChartData}>
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
              tickLine={false}
              axisLine={false}
              allowDataOverflow={false}
              tickMargin={8}
              tickFormatter={(value) => `${value} zł`}
              domain={([dataMin, dataMax]) => [
                Math.floor(dataMin / 50) * 50,
                Math.ceil(dataMax / 50) * 50,
              ]}
            />
            <XAxis
              dataKey="date"
              tickLine={false}
              axisLine={false}
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
                  valueFormatter={(value) => {
                    const escapedValue = Number(value.replace(/,/g, "."));
                    return formatCurrency(escapedValue, "PLN");
                  }}
                />
              }
            />
            {isChartVisible &&
              shopKeys.map((shopKey) => (
                <Area
                  key={shopKey}
                  dataKey={shopKey}
                  type="monotone"
                  fill={`url(#fill${shopKey.replace(/\s+/g, "")})`}
                  stroke={chartConfig[shopKey]?.color}
                  strokeWidth={2}
                />
              ))}
            <ChartLegend content={<ChartLegendContent />} />
          </AreaChart>
        </ChartContainer>
      </DialogContent>
    </Dialog>
  );
};
