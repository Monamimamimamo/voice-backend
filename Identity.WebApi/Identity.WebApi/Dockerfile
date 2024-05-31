# Базовый образ
FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS base

# Пользователь и рабочая директория
USER app
WORKDIR /app

# Открытие портов
EXPOSE 8080
EXPOSE 8081

# Тома
VOLUME /home/app/.microsoft/usersecrets
VOLUME /home/app/.aspnet/https

# Сборка
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
ARG BUILD_CONFIGURATION=Release
WORKDIR /src
COPY ["Identity.WebApi/Identity.WebApi.csproj", "Identity.WebApi/"]
RUN dotnet restore "./Identity.WebApi/Identity.WebApi.csproj"
COPY . .
WORKDIR "/src/Identity.WebApi"
RUN dotnet build "./Identity.WebApi.csproj" -c $BUILD_CONFIGURATION -o /app/build

# Публикация
FROM build AS publish
ARG BUILD_CONFIGURATION=Release
RUN dotnet publish "./Identity.WebApi.csproj" -c $BUILD_CONFIGURATION -o /app/publish /p:UseAppHost=false

# Финальный этап
FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .

# Копирование данных в тома
COPY ./Identity.WebApi/UserSecrets /home/app/.microsoft/usersecrets
COPY ./Identity.WebApi/ASP.NET/Https /home/app/.aspnet/https

ENTRYPOINT ["dotnet", "Identity.WebApi.dll"]
