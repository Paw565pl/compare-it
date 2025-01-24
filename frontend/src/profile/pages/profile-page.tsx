"use client";

import { Button } from "@/core/components/ui/button";
import { PriceAlertsGrid } from "@/profile/components/price-alerts-grid";
import { UserDetailsCard } from "@/profile/components/user-details-card";
import { AlarmClock, Heart, NotebookPen, User } from "lucide-react";
import { ReactNode, useState } from "react";

interface AsideButton {
  readonly title: string;
  readonly component: ReactNode;
  readonly icon: ReactNode;
}

const asideButtonClassName = "!w-8 !h-8 !sm:w-10 !sm:h-10" as const;

export const ProfilePage = () => {
  const [activeComponent, setActiveComponent] = useState<ReactNode>(
    <UserDetailsCard />,
  );

  const asideButtons: AsideButton[] = [
    {
      title: "Twój profil",
      component: <UserDetailsCard />,
      icon: <User className={asideButtonClassName} />,
    },
    {
      title: "Ulubione produkty",
      component: <div>Ulubione produkty</div>,
      icon: <Heart className={asideButtonClassName} />,
    },
    {
      title: "Zarządzanie alertami",
      component: <PriceAlertsGrid />,
      icon: <NotebookPen className={asideButtonClassName} />,
    },
    {
      title: "Historia alertów",
      component: <div>Historia alertów</div>,
      icon: <AlarmClock className={asideButtonClassName} />,
    },
  ];

  return (
    <div className="flex flex-col gap-2 sm:flex-row sm:gap-8">
      <aside className="mb-6 flex gap-2 sm:mb-0 sm:w-1/6 sm:flex-col sm:gap-4">
        {asideButtons.map(({ title, component, icon }, index) => (
          <Button
            key={index}
            onClick={() => setActiveComponent(component)}
            variant="outline"
            size="icon"
            title={title}
            className="w-full p-6 sm:p-8"
          >
            {icon}
          </Button>
        ))}
      </aside>

      <section className="w-full">{activeComponent}</section>
    </div>
  );
};
